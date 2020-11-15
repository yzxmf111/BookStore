package com.bookstore.controller.background;

import com.google.common.collect.Maps;
import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Product;
import com.bookstore.pojo.User;
import com.bookstore.service.IFileService;
import com.bookstore.service.IProductService;
import com.bookstore.utils.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/21 20:56
 */

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * @Author: Tian
     * @Description: 保存或者更新的方法
     * @Params:
     * @Return:
     */
    @RequestMapping("/save.do")
    @ResponseBody
    public ServerResponse saveProduct(HttpSession session, Product product) {
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
            return iProductService.addOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 产品上下架
     * @Params:
     * @Return:
     */
    @RequestMapping("/set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
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
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 获取产品详情
     * @Params:
     * @Return:
     */

    @RequestMapping("/detail.do")
    @ResponseBody
    public ServerResponse getProductDetail(HttpSession session, Integer productId) {
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
            return iProductService.getProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 获取产品列表, 使用mybatis的page-helper辅助分页功能的实现,需要传递过去以下参数
     * @Params: pageNum:当前页码,pageSize 每页展示条数
     * @Return:
     **/
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse getProductList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
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
            return iProductService.getProductList(pageNum, pageNum);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 产品搜索
     * @Params:
     * @Return:
     **/
    @RequestMapping("/search.do")
    @ResponseBody
    public ServerResponse searchProduct(HttpSession session, Integer productId, String pruductName, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
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
            return iProductService.searchProduct(productId, pruductName, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 文件上传
     * @Params:
     * @Return:
     **/
    @RequestMapping("/upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        //判断session是否有对象,是否需要强制登录
        //必须做判断,防止恶意用户,反复上传10m的文件
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
            //这里的路径默认指的是 weapp下的路径,可以不存在 upload文件夹,我们接下来在service里边创建
            String realPath = request.getServletContext().getRealPath("upload");
            String uri = iFileService.upload(file, realPath);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + "" + uri;

            HashMap<String, String> map = Maps.newHashMap();
            map.put("uri", uri);
            map.put("url", url);
            return ServerResponse.createBySuccess(map);
        } else {
            return ServerResponse.createByErrorMessage("非管理员,无操作权限");
        }
    }

    /**
     * @Author: Tian
     * @Description: 富文本上传图片
     * @Params:
     * @Return:
     **/
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        //判断session是否有对象,是否需要强制登录
        //必须做判断,防止恶意用户,反复上传10m的文件
        //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
        //    {
        //      "success": true/false,
        //      "msg": "error message", # optional
        //      "file_path": "[real file path]"
        //   }
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("error message", "用户未登录");
            return resultMap;
        }
        //判断是否是管理员,可以单独写一个校验方法,因为后台的操作都要先判断角色再进行操作
        //这样可能更加优雅,但是以下写法更加直观
        if (user.getRole() == (Const.Role.ROLE_ADMIN)) {
            //是管理员
            //增加我们处理分类的逻辑
            String realPath = request.getServletContext().getRealPath("upload");
            String uri = iFileService.upload(file, realPath);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + "" + uri;

            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            //很多前端的插件对后端的返回值的响应头有要求,所以要修改相应的响应头
            // response.addHeader("Access-Control-Allow-Headers","X-File-Name");这是富文本上传的规范.只处理上传成功的

            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("error message", "非管理员,无操作权限");
            return resultMap;

        }
    }

}

