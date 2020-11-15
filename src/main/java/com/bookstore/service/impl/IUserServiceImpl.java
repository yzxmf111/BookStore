package com.bookstore.service.impl;

import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.common.TokenCache;
import com.bookstore.dao.UserMapper;
import com.bookstore.pojo.User;
import com.bookstore.service.IUserService;
import com.bookstore.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/14 23:24
 */

@Service("iUserService")
public class IUserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> selectLogin(String username, String password) {
        int userCount = userMapper.checkUsername(username);
        if (userCount == 0) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //todo MD5密码加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //让user返回值拿不到密码
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        user.setAnswer(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse<String> response = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!response.isSuccess()) {
            return response;
        }
        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!response.isSuccess()) {
            return response;
        }
        //设置角色,我们可以使用枚举,但是两个角色使用枚举显得过于繁重,
        // 所以我们使用一个内部接口类,对常量进行分组,他没有枚举那么重
        user.setRole(Const.Role.ROLE_CUSTOMER);
        String md5Password = MD5Util.MD5EncodeUtf8(user.getPassword());

        int count = userMapper.insert(user);
        if (count == 0) {
            ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");
    }

    //因为以后会经常校验用户名,所以才定义了这个方法,当然这应该在看接口文档的时候就提前想到
    public ServerResponse<String> checkValid(String str, String type) {
        //str是真实的用户名或者邮箱,type是username或者email
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            //以下开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccess("校验通过");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        //yinggai先进行用户名字的校验
        ServerResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.forgetGetQuestion(username);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);

        }
        return ServerResponse.createByErrorMessage("该用户未设置找回密码问题");
    }

    public ServerResponse<String> getAnswer(String username, String question, String answer) {
        int resultCount = userMapper.getAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题及问题答案是这个用户的,并且是正确的
            //我们需要一个标志forgetToken,标记这个用户可以修改密码
            //没有forgetToken的话，也就无法确定修改哪个用户的密码了，
            // 这个是用来标记具体的用户的。,这样才能一一对应
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);

            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String token, String newPassword) {
        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String forgetToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (org.apache.commons.lang3.StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("token已经失效了");
        }
        //StringUtils的equals方法,插进去null也无所谓.更加安全
        if (org.apache.commons.lang3.StringUtils.equals(token, forgetToken)) {
            //新密码进行加密后就可以在数据库中进行更新了
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (resultCount > 0) {
                return ServerResponse.createBySuccess("修改密码成功");
            } else {
                return ServerResponse.createByErrorMessage("修改密码失败");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误,请重新输入");
        }
       /* //下面这行代码可以不要
        return ServerResponse.createByErrorMessage("修改密码失败");*/
    }

    public ServerResponse<String> resetPassword(String oldPassword, User user, String newPassword) {

        //先校验旧的密码,db中存的都是MD5加密过的,所以,这里也要先加密
        String MD5OldPassword = MD5Util.MD5EncodeUtf8(oldPassword);
        int resultCount = userMapper.checkPassword(MD5OldPassword);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("该密码不存在");
        }
        int count = userMapper.checkPasswordAndId(user.getId(), oldPassword);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("用户密码与用户id不一致,请从新输入");
        }
        //String MD5NewPasswor = MD5Util.MD5EncodeUtf8(newPassword);
        //用户名是无法更新的
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int result = userMapper.updateByPrimaryKeySelective(user);
        if (result > 0) {
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<User> updateUserInfo(User user) {
        //用户名密码在这里是不能修改的,对应的前台页面也没有这些选项
        //email也要进行一个校验,校验新的email是不是已经存在,并且不校验当前这个用户的emil.只查和别人的emil是否相同
        //todo:用户名一个月只能修改一次
        int count = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (count > 0) {
            return ServerResponse.createByErrorMessage("用户邮箱已存在,请重新输入");
        }

      /*  //感觉多余
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        //我们只更新以上几个字段,updateTime在代码中不用关心*/
        //以上代码,感觉多余了,因为传过来user中应该已经设置好了这些东西
        int resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("更新成功", user);
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }
    
    /**
    * @Author: Tian
    * @Description: 判断是不是管理员
    * @Params: 
    * @Return: 
     */
}
