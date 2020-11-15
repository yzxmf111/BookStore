package com.bookstore.controller.background;

import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/19 12:00
 */

@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    /**
    * @Author: Tian
    * @Description: 用户登录接口
    * @Params:
    * @Return:
     */
    @RequestMapping("/login.do")
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.selectLogin(username, password);
        if (response.isSuccess()) {
            if (response.getData().getRole().equals(Const.Role.ROLE_ADMIN))
                session.setAttribute(Const.CURRENT_USER, response.getData());
        }
            return ServerResponse.createByErrorMessage("用户非管理员,请重新登陆");
    }




}
