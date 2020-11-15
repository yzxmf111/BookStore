package com.bookstore.dao;

import com.bookstore.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    //以上是mybatis-plugins生成的接口,下边进行我们自己接口的开发
    //获取所有的平级子节点
    List<Category> selectByPrimaryKeyId(Integer parentId);



}