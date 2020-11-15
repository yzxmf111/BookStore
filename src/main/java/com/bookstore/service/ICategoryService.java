package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Category;

import java.util.List;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/14 23:17
 */

public interface ICategoryService {

    public ServerResponse addCategory(Integer parent_id, String catagoryName);

    public ServerResponse<String> setCategoryName(Integer id, String categoryName);

    public ServerResponse<List<Category>> getChildrenCategory(Integer categoryId);

    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId);
}
