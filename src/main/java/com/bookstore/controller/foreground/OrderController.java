package com.bookstore.controller.foreground;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/31 11:39
 */

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("/create.do")
    @ResponseBody
    public ServerResponse createOrder(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(), shippingId);
    }

    /**
     * @Author: Tian
     * @Description: 取消订单, 防止横向越权
     * @Params:
     * @Return:
     **/
    @RequestMapping("/cancel.do")
    @ResponseBody
    public ServerResponse cancelOrder(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(), orderNo);
    }

    /**
     * @Author: Tian
     * @Description: 获取订单的商品信息
     * @Params:
     * @Return:
     **/
    @RequestMapping("/get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderProductMsg(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderProductMsg(user.getId());
    }

    /**
     * @Author: Tian
     * @Description: 订单详情detail
     * @Params:
     * @Return:
     **/
    @RequestMapping("/detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    /**
     * @Author: Tian
     * @Description: 订单List
     * @Params:
     * @Return:
     **/
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse getOrderList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderVoList(user.getId(), pageNum, pageNum);
    }


    //支付宝相关

    /**
     * @Author: Tian
     * @Description: 支付宝支付
     * @Params:
     * @Return:
     **/
    @RequestMapping("/pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, HttpServletRequest request, Long orderNo) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String realPath = request.getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(), orderNo, realPath);
    }

    /**
     * @Author: Tian
     * @Description: 查询订单支付状态
     * @Params:
     * @Return:
     **/
    @RequestMapping("/query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderStatue(HttpSession session, Long orderNo) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse response = iOrderService.queryOrderStatue(user.getId(), orderNo);
        if (response.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

    /**
     * @Author: Tian
     * @Description: 支付宝回调, 支付宝的回调(支付成功)会把所有的参数以post方式封装在request里边, 需要我们自己获取,
     * @Params:
     * @Return:
     **/
    @RequestMapping("/alipay_callback.do")
    @ResponseBody
    public Object callback(HttpServletRequest request) {

        //创建一个map用于存放我们从request中获得的信息
        Map<String, String> params = Maps.newHashMap();
        Map<String, String[]> requestParams = request.getParameterMap();
        Set<String> keySet = requestParams.keySet();
        for (String key : keySet) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            //现在想要把数组values组合起来
            for (int i = 0; i < values.length; i++) {
                valueStr = i == values.length - 1 ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //从request中拿到了参数
            params.put(key, valueStr);
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());
        //接下来,验签,这非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.

        //在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数。
        //必须移除 sign_type 与  sign 而sign sdk已经帮我们移除了
        params.remove("sign_type");

        //使用AlipaySignature的rsaCheckV2方法进行验签 不通过无法执行下边的内容
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!alipayRSACheckedV2) {
                return ServerResponse.createByErrorMessage("非法请求,验证不通过,再恶意请求我就报警找网警了");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常", e);
        }

        //需要严格按照如下描述校验通知数据的正确性。 验证各种数据 在service里边验证
        /*商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号；
        判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额）；
        校验通知中的 seller_id（或者seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
        （有的时候，一个商户可能有多个 seller_id/seller_email）。
        上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
        在上述验证通过后商户必须根据支付宝不同类型的业务通知，
        正确的进行不同的业务处理，并且过滤重复的通知结果数据。
        在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。*/
        //这个只验证了一部分 todo 验证各种数据
        ServerResponse response = iOrderService.aliCallback(params);
        if (response.isSuccess()) {
            //只有返回给前端success,才不会继续回调,这个是alipay的规定
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }
}

