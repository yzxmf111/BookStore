package com.bookstore.controller.foreground;

import com.github.pagehelper.PageInfo;
import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Shipping;
import com.bookstore.pojo.User;
import com.bookstore.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/29 22:20
 */

@Controller
@RequestMapping("/shipping")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;
    /**
    * @Author: Tian
    * @Description: 添加地址
    * @Params:
    * @Return:
     **/
    @RequestMapping("/add.do")
    @ResponseBody
    //我们shipping数据表的id是自增增长的,我们希望获取他,并返回给前端,
    //     *              就要在xml文件中进行相应配置,自动生成主键
    public ServerResponse add(HttpSession session,Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
       return iShippingService.add(user.getId(),shipping);
    }
    /**
    * @Author: Tian
    * @Description: 删除地址,防止横向越权(在数据库多加一个user_id限定条件)
    * @Params:
    * @Return:
     **/
    @RequestMapping("/del.do")
    @ResponseBody
    public ServerResponse ldel(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iShippingService.del(user.getId(),shippingId);
    }
    /**
     * @Author: Tian
     * @Description: 登录状态更新地址,防止横向越权(在数据库多加一个user_id限定条件)
     * @Params:
     * @Return:
     **/
    @RequestMapping("/update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session,Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iShippingService.update(user.getId(),shipping);
    }
    /**
    * @Author: Tian
    * @Description: 选中查看具体的地址,防止横向越权(在数据库多加一个user_id限定条件)
    * @Params:
    * @Return:
     **/
    @RequestMapping("/select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录");
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    /**
    * @Author: Tian
    * @Description: 地址列表
    * @Params:
    * @Return:
     **/
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getShippingList(HttpSession session, @RequestParam(value="pageNum",defaultValue= "1" )Integer pageNum,
                                          @RequestParam(value="pageSize",defaultValue= "10" )Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录,请登录之后查询");
        }
        return iShippingService.getShippingList(user.getId(),pageNum,pageSize);
    }
}
