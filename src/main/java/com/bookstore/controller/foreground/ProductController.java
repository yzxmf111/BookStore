package com.bookstore.controller.foreground;

import com.github.pagehelper.PageInfo;
import com.bookstore.common.ServerResponse;
import com.bookstore.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/26 18:39
 */

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse getProductDetail(Integer productId) {

        return iProductService.userGetProductDetail(productId);
    }

    @RequestMapping("/detail.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductDetail(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                     @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                     @RequestParam(value = "orderby", defaultValue = "") String orderby) {

        return iProductService.getProductByKeywordAndcategoryId(keyword,categoryId,pageNum,pageSize,orderby);
    }
}
