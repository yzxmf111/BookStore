package com.bookstore.controller.foreground;

import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/14 23:16
 */

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * @Author: Tian
     * @Description: 登录功能的开发
     * @Params:
     * @Return:
     */
    @RequestMapping(value = "/login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.selectLogin(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * @Author: Tian
     * @Description: 登出, 注册, 校验功能的开发
     * @Params:
     * @Return:
     */
    @RequestMapping(value = "/logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logOut(HttpSession session) {
        if (session == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess("登出成功");
    }

    @RequestMapping(value = "/register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        //这里应该发送异步请求,进行校验
//        ServerResponse<User> response = iUserService.register(user);
//        if (response.isSuccess()){
//            return response;
//        }
//        return response;
//    }以上的写法虽然也可以,但是属于多此一举
        return iUserService.register(user);
    }
    //接口:检查用户名,邮箱是否有效
    @RequestMapping(value = "/check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse check_valid(String str, String type){
       /* ServerResponse<String> response = iUserService.checkValid(str, type);
        return response;*/
       return iUserService.checkValid(str, type);
    }

    /**
    * @Author: Tian
    * @Description: 获取用户登录信息
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorMessage(" 用户未登录,无法获取当前用户信息");
        }

        user.setAnswer(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
    * @Author: Tian
    * @Description: 用户忘记密码
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){

       return iUserService.forgetGetQuestion(username);
    }
    
    /**
    * @Author: Tian
    * @Description: 提交问题答案及验证
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){

        return iUserService.getAnswer(username, question, answer);
    }

    /**
    * @Author: Tian
    * @Description: 忘记密码的重设密码
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRresetPassword(String username, String token, String newPassword){

        return iUserService.forgetResetPassword(username,token,newPassword);
    }

    /**
    * @Author: Tian
    * @Description: 登录状态下重新设置密码(需要验证旧密码,用户ID,不然恶意用户可以拿着字典/碰撞 出来一个密码,就和把其他用户的密码修改了)
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String oldPassword,String  newPassword,HttpSession session){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorMessage(" 用户未登录");
        }
        return iUserService.resetPassword(oldPassword,user,newPassword);
    }

    /**
    * @Author: Tian
    * @Description: 登录状态更新个人信息
    * @Params:
    * @Return:
     */
    @RequestMapping(value = "/update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session, User user){
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null){
            return ServerResponse.createByErrorMessage(" 用户未登录");
        }
        user.setUsername(sessionUser.getUsername());
        user.setId(sessionUser.getId());
        ServerResponse<User> response = iUserService.updateUserInfo(user);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * @Author: Tian
     * @Description: 强制登录,跟前端约定,一旦我们传10过去,就会进行强制登录,并获取用户的详细信息
     * @Params:
     * @Return:
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息,status=10,强制登录");
        }
        //session里边已经有了user的信息,但是不能保证里边的信息是最新的,因为存session可能出错,最为稳妥的办法是再查一遍
        //以防别人查到问题和答案,将答案置为空
        currentUser.setAnswer(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(currentUser);
    }


}
