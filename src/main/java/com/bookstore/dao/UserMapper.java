package com.bookstore.dao;

import com.bookstore.pojo.User;
import org.apache.ibatis.annotations.Param;


public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //以上是mybatis-plugins生成的接口,下边进行我们自己接口的开发
    int checkUsername(String username);

    //@param的作用:起别名,传入多个参数
    User selectLogin(@Param("username") String username, @Param("password") String password);

    int checkEmail(String email);

    String forgetGetQuestion(String username);

    int getAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    //这里更新会用返回值吗?
    int updatePasswordByUsername(@Param("username") String username, @Param("newPassword") String newPassword);

    int checkPassword(String password);

    int checkPasswordAndId(@Param("id")int id,@Param("password")String password);

    int checkEmailByUserId(String email ,int id);
}