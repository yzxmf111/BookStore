package com.bookstore.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.*;
import com.bookstore.pojo.*;
import com.bookstore.service.IOrderService;
import com.bookstore.utils.BigDecimalUtil;
import com.bookstore.utils.DateTimeUtil;
import com.bookstore.utils.FTPUtil;
import com.bookstore.utils.PropertiesUtil;

import com.bookstore.vo.OrderItemVo;
import com.bookstore.vo.OrderProductVo;
import com.bookstore.vo.OrderVo;
import com.bookstore.vo.ShippingVo;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import java.util.*;


/**
 * @description:
 * @author: Tian
 * @time: 2020/7/31 11:40
 */
@Service("iOrderService")
public class IOrderServiceImpl implements IOrderService {

    private Logger logger = LoggerFactory.getLogger(IOrderServiceImpl.class);

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private Order order;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //够我们生成订单,是为购物车里勾选的那些商品生成的 这里获得的是购物车里勾选的商品列表
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //创建私有方法,获取OrderItem的集合.并将OrderItem-->OrderItemVo
        ServerResponse<List<OrderItem>> response = this.getOrderItemByCartList(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = response.getData();
        //获取订单中需要支付的总价
        BigDecimal payment = this.getPayment(orderItemList);
        //定义私有方法生成订单
        Order order = this.getOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis 批量插入
        orderItemMapper.batchInsert(orderItemList);
        //生成成功,我们要减少我们产品的库存
        this.reduceProductStock(orderItemList);
        // 清空一下购物车
        this.cleanCart(cartList);
        //将OrderItem-->OrderItemVo
     /*  List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.getOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo)
        }*/
        OrderVo orderVo = this.getOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }


    public ServerResponse cancelOrder(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
                return ServerResponse.createByErrorMessage("此订单已付款，无法被取消");
            }
            //int count = orderMapper.deleteByPrimaryKey(order.getId());
            //并不从数据库中删除订单,而是更新订单状态
//            if (count > 0 ){
//                return ServerResponse.createBySuccess();
//            }
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
            int count = orderMapper.updateByPrimaryKeySelective(updateOrder);
            if (count > 0) {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByError();
        }
        return ServerResponse.createByErrorMessage("该用户没有此订单");
    }

    public ServerResponse getOrderProductMsg(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //够我们生成订单,是为购物车里勾选的那些商品生成的 这里获得的是购物车里勾选的商品列表
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //创建私有方法,获取OrderItem的集合.并将OrderItem-->OrderItemVo
        ServerResponse<List<OrderItem>> response = this.getOrderItemByCartList(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = response.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.getOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        BigDecimal payment = this.getPayment(orderItemList);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("bookstore.properties"));
        orderProductVo.setProductTotalPrice(payment);
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(userId, orderNo);
            OrderVo orderVo = this.getOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("没有找到订单");
    }

    public ServerResponse<PageInfo> getOrderVoList(Integer userId, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        //获取orderList,并将orderList中的order->orderVo
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(userId, orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(Integer userId, List<Order> orderList) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Order order : orderList) {
            //List<OrderItem> orderItemList = Lists.newArrayList();
            //要判断是否是管理员
            if (userId == null) {
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByOrderNoAndUserId(userId, order.getOrderNo());
            }
            OrderVo orderVo = this.getOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    //private OrderVo getOrderVo
    private OrderVo getOrderVo(Order order, List<OrderItem> orderItemList) {

        OrderVo orderVo = new OrderVo();

        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.ONLINE_PAY.getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatue(order.getStatus());
        orderVo.setStatusDesc(Const.ProductStatusEnum.ON_SALE.getValue());

        orderVo.setPaymentTime(order.getPaymentTime());
        orderVo.setSendTime(order.getSendTime());
        orderVo.setEndTime(order.getEndTime());
        orderVo.setCloseTime(order.getCloseTime());
        orderVo.setCreateTime(order.getCreateTime());

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.getOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.getShippingVo(shipping));
        }
        return orderVo;
    }

    private ShippingVo getShippingVo(Shipping shipping) {
        //自动注入???-->与设计模式有关
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    //获取订单中需要支付的总价
    private BigDecimal getPayment(List<OrderItem> orderItemList) {

        BigDecimal payment = new BigDecimal(0);
        if (orderItemList == null) {
            return payment;
        }
        for (OrderItem orderItem : orderItemList) {
            //BigDecimal不可以使用普通的 + - * / 而是要用 add subtract multiply and divide
            // payment = payment.add(orderItem.getTotalPrice());
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    //定义私有方法生成订单
    private Order getOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        //可否自动注入
        // Order order = new Order();
        //需要定义新方法生成 orderNo
        order.setOrderNo(this.generateOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(1);
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        //付款时间,发货时间等等
        int count = orderMapper.insert(order);
        if (count > 0) {
            return order;
        }
        return null;
    }

    //需要定义新方法生成 orderNo.
    //订单号生成的规则:不能被竞争对手识破.查 如何实现(现编的比较简单)
    //以下的订单号对于高并发 可能有人会创建不成功
    private Long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        currentTime += new Random().nextInt(100);
        return currentTime;
    }

    //定义私有方法将OrderItem-->OrderItemVo
    private OrderItemVo getOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreatTime(orderItem.getCreateTime());
        return orderItemVo;
    }

    ////定义私有方法获取 List<OrderItem>
    private ServerResponse<List<OrderItem>> getOrderItemByCartList(Integer userId, List<Cart> cartList) {
        //现在我们进行校验\
        if (CollectionUtils.isEmpty(cartList)) {

            return ServerResponse.createByErrorMessage("购物车为空");
        }
        List<OrderItem> orderItems = Lists.newArrayList();
        //因为购物车中的商品可能是以前加入的,所以现在要判断cartList中商品的状态和数量
        for (Cart cart : cartList) {
            //查询商品是否在售
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());

            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createByErrorMessage("产品已下架");
            }
            //库存可能与以前加入购物车时相比发生了变化
            if (product.getStock() < cart.getQuantity()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "数量不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));
            orderItems.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItems);
    }


    //后台service
    public ServerResponse<PageInfo> getManageOrderList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.getAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(null, orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrderVo> getOrderDetail(Long orderNo) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("请输入订单号");
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = getOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo> search(Long orderNo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("请输入订单号");
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = getOrderVo(order, orderItemList);
        //为了做分页,将orderVo->List<orderVo>
        List<OrderVo> orderVoList = Lists.newArrayList(orderVo);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse sendGoods(Long orderNo) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("请输入订单号");
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                int count = orderMapper.updateByPrimaryKeySelective(order);
                if (count > 0) {
                    return ServerResponse.createBySuccessMessage("发货成功");
                } else {
                    return ServerResponse.createByErrorMessage("发货失败");
                }
            }
            return ServerResponse.createByErrorMessage("用户未付款");

        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }


    //支付宝相关业务逻辑层方法
    public ServerResponse pay(Integer userId, Long orderNo, String realPath) {
        HashMap<String, String> map = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        map.put("orderNo", orderNo.toString());
        //下边是生成支付宝二维码的各种参数,参考官方demo(二维码也包含了这些参数)
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orderNo.toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("华理互助书城扫码支付,订单:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = String.valueOf(order.getPayment());

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单:").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            //支付宝以分为单位
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(), orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //注意：setNotifyUrl回调地址的地方一定是外网的地址，本机不可用
                //
                //这是扫码支付成功后,支付宝会将该笔订单的变更信息，沿着以下异步通知路径, 通过 POST 请求的形式将支付结果作为参数通知到商户系统。-->回调
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        //商户发起预下单请求
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                // 简单打印应答
                dumpResponse(response);
                //生成二维码,并且上传到图片服务器,最后拼接成url返回给前端
                File fileDir = new File(realPath);
                if (!fileDir.exists()) {
                    //tomcat的权限设置
                    fileDir.setWritable(true);
                    fileDir.mkdirs();
                }
                // 需要修改为运行机器上的路径
                //细节细节细节  斜杠path+"/qr-%s.png,运行在线上,所以是path---不加斜杠的话c:upload/imageqr-%s.png
                //%s是替换符,response.getOutTradeNo()会对其进行替换
                String qrPath = String.format(realPath + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());

                /**
                 *当前预下单请求生成的二维码码串，使用了google zxing作为二维码生成工具生成二维码,
                 * 将内容contents生成长宽均为width的图片，图片路径由imgPath指定
                 */
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                logger.info("filePath:" + qrPath);
                //上传图片到ftp服务器
                File targetFile = new File(qrPath);
                try {
                    //可以看源码 -->切换了工作目录到image,再把targetFile以流的形式存进去,名字就是targetFile.getName
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常", e);
                }
                String qrUrl = PropertiesUtil.getProperty("http://img.happymmall.com/") + targetFile.getName();
                map.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(map);


            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse queryOrderStatue(Integer userId, Long orderNo) {

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
                return ServerResponse.createBySuccess();
            }
        }
        return ServerResponse.createByErrorMessage("该用户并没有该订单,查询无效");
    }

    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatue = params.get("trade_statue");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("非华理二手书城的订单,回调忽略");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatue)) {

            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //验签 且验证数据的正确性后 持久化支付信息到商家数据库中
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatue);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }
}
