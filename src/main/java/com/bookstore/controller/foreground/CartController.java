package com.bookstore.controller.foreground;

import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.ICartService;
import com.bookstore.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


/**
 * @description: 购物车模块
 * @author: Tian
 * @time: 2020/7/27 16:37
 */

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * @Author: Tian
     * @Description: 增
     * @Params:
     * @Return:
     **/
    @RequestMapping("/add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer productId, Integer count) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.add(user.getId(), productId, count);
    }

    /**
    * @Author: Tian
    * @Description: 删 --防止横向越权,只能删除自己购物车的商品,userId和productIdss两个条件同时满足
    * @Params:
    * @Return:
     **/
    @RequestMapping("/delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> delete(HttpSession session, String productIds) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.delete(user.getId(), productIds);
    }

    /**
     * @Author: Tian
     * @Description: 改 --防止横向越权,只能更改自己购物车的商品,userId和productIdss两个条件同时满足
     * @Params:
     * @Return:
     **/
    @RequestMapping("/update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer productId,Integer count) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.update(user.getId(),productId,count);
    }

    /**
     * @Author: Tian
     * @Description: 查
     * @Params:
     * @Return:
     **/
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.list(user.getId());
    }

    /**
    * @Author: Tian
    * @Description: 购物车选中某个商品,我们使用一个高复用的service和dao接口实现4个方法
    * @Params:
    * @Return:
     **/
    @RequestMapping("/select.do")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpSession session,Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    /**
     * @Author: Tian
     * @Description: 购物车取消选中某个商品,我们使用一个高复用的service和dao接口实现4个方法
     * @Params:
     * @Return:
     **/
    @RequestMapping("/un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * @Author: Tian
     * @Description: 购物车 "选中所有"  商品,我们使用一个高复用的service和dao接口实现4个方法
     * @Params:
     * @Return:
     **/
    @RequestMapping("/select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session,Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }
    /**
     * @Author: Tian
     * @Description: 购物车 "全不选" 商品,我们使用一个高复用的service和dao接口实现4个方法
     * @Params:
     * @Return:
     **/
    @RequestMapping("/un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session,Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    /**
    * @Author: Tian
    * @Description: 查询在购物车里的产品数量,没有用户登录也不报错
    * @Params:
    * @Return:
     **/
    @RequestMapping("/get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
