package com.bookstore.service;

import com.github.pagehelper.PageInfo;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Product;
import com.bookstore.vo.ProductDetailVo;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/21 20:56
 */
public interface IProductService {

    public ServerResponse<String> addOrUpdateProduct(Product product);

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    public ServerResponse<PageInfo> searchProduct(Integer productId, String productName,Integer pageNum,Integer pageSize);

    public ServerResponse<ProductDetailVo> userGetProductDetail(Integer productId);

    public ServerResponse<PageInfo> getProductByKeywordAndcategoryId(String keyword, Integer categoryId, Integer pageSize,
                                                                     Integer pageNum, String orderby);
}
