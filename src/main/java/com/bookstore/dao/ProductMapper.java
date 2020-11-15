package com.bookstore.dao;

import com.bookstore.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();

    List<Product> searchProduct(@Param(value = "id")Integer id,@Param(value = "productName") String productName);

    List<Product> getProductByKeywordAndcategoryId(@Param(value = "categoryIdList")List<Integer> categoryIdList,@Param(value = "keyword")String keyword);
}