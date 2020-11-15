package com.bookstore.controller.background;

import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @description:
 * @author: Tian
 * @time: 2020/8/3 10:03
 */

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IOrderService iOrderService;

    /**
    * @Author: Tian
    * @Description: 获取所有的订单vo列表
    * @Params:
    * @Return:
     **/
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse getManageOrderList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iOrderService.getManageOrderList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 按照订单号查询, 现在是精确订单号匹配,但是我们要做分页,为以后功能扩展做准备.todo 做用户,名字模糊匹配,复合查询做准备做准备
     * @Params:
     * @Return:
     **/
    @RequestMapping("/search.do")
    @ResponseBody
    public ServerResponse search(HttpSession session, Long orderNo, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iOrderService.search(orderNo, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 获得订单详情
     * @Params:
     * @Return:
     **/
    @RequestMapping("/detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iOrderService.getOrderDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 订单发货
     * @Params:
     * @Return:
     **/
    @RequestMapping("/send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session, Long orderNo) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iOrderService.sendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }
}