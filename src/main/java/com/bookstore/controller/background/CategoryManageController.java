package com.bookstore.controller.background;

import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.bookstore.service.ICategoryService;
import com.bookstore.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/20 12:04
 */

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * @Author: Tian
     * @Description: 增加品类节点,
     * @Params:
     * @Return:
     */
    @RequestMapping("/add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, @RequestParam(value = "parent_id", defaultValue = "0") Integer parent_id, String categoryName) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            //在后台的话,这里要强制登录,前台不用强制登录,采用我们和前台的约定
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员,可以单独写一个校验方法,因为后台的操作都要先判断角色再进行操作
        //这样可能更加优雅,但是以下写法更加直观
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iCategoryService.addCategory(parent_id, categoryName);
        }else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 更新物品名字
     * @Params:
     * @Return:
     */
    @RequestMapping("/set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            //在后台的话,这里要强制登录,前台不用强制登录,采用我们和前台的约定
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员,可以单独写一个校验方法,因为后台的操作都要先判断角色再进行操作
        //这样可能更加优雅,但是以下写法更加直观
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iCategoryService.setCategoryName(categoryId, categoryName);
        }else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 获取当前节点的平级子节点, 不在向下递归
     * @Params:
     * @Return:
     */
    @RequestMapping("/get_category.do")
    @ResponseBody
    public ServerResponse getCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            //在后台的话,这里要强制登录,前台不用强制登录,采用我们和前台的约定
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员,可以单独写一个校验方法,因为后台的操作都要先判断角色再进行操作
        //这样可能更加优雅,但是以下写法更加直观
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iCategoryService.getChildrenCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 获取当前分类id及递归子节点的categoryId
     * @Params:
     * @Return:
     */
    @RequestMapping("/get_deep_category.do")
    @ResponseBody
    public ServerResponse getDeepCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        //判断session是否有对象,是否需要强制登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            //在后台的话,这里要强制登录,前台不用强制登录,采用我们和前台的约定
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,强制登录");
        }
        //判断是否是管理员,可以单独写一个校验方法,因为后台的操作都要先判断角色再进行操作
        //这样可能更加优雅,但是以下写法更加直观
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            return iCategoryService.getDeepCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }
}
