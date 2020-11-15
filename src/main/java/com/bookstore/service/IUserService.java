package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/14 23:17
 */

public interface IUserService {

    ServerResponse<User> selectLogin(String username, String password);

    public ServerResponse<String> register(User user);

    public ServerResponse<String> checkValid(String str, String type);

    public ServerResponse<String> forgetGetQuestion(String username);

    public ServerResponse<String> getAnswer(String username, String question, String answer);

    public ServerResponse<String> forgetResetPassword(String username, String token, String newPassword);

    public ServerResponse<String> resetPassword(String oldPassword, User user, String newPassword);

    public ServerResponse<User> updateUserInfo(User user);
}
