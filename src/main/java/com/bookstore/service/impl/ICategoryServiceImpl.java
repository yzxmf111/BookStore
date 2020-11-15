package com.bookstore.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.CategoryMapper;
import com.bookstore.pojo.Category;
import com.bookstore.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/14 23:24
 */

@Service("iCategoryService")
public class ICategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(ICategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private Category category;

    //  ServerResponse后边不写<T><User><String> 就相当于里边是<Object>,可以接受一切类型,当然如果要向下转型的话就可能出现错误,所以最好还是写上
    public ServerResponse addCategory(Integer parent_id, String categoryName) {
        //parent_id如果前端没有传值的时候,有一个默认值0
        if (org.apache.commons.lang3.StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        this.category.setId(parent_id);
        this.category.setName(categoryName);
        this.category.setStatus(true);//这个分类是可用的,true和数据库的数据类型int可以进行转换

        int count = categoryMapper.insert(this.category);
        if (count > 0) {
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");

    }

    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || org.apache.commons.lang3.StringUtils.isBlank(categoryName)) {
            ServerResponse.createByErrorMessage("参数错误");
        }
        //根据id查询出商品
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            category.setName(categoryName);
            int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
            if (resultCount > 0) {
                return ServerResponse.createBySuccessMessage("更新品类名字成功");
            }
            return ServerResponse.createByErrorMessage("更新品类名字失败");
        }
        return ServerResponse.createByErrorMessage("产品id不存在,请重新输入");
    }

    public ServerResponse<List<Category>> getChildrenCategory(Integer categoryId) {
        if (categoryId == null) {
            ServerResponse.createByErrorMessage("参数错误");
        }
        //根据id查询出商品子类
        List<Category> category = categoryMapper.selectByPrimaryKeyId(categoryId);
        if (category != null) {
            return ServerResponse.createBySuccess(category);
        }
        return ServerResponse.createByErrorMessage("未找到当前分类的子分类");
    }
    /*  if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }
    代码也可以这样实现
    */

    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId) {
        if (categoryId == null) {
            ServerResponse.createByErrorMessage("参数错误");
        }
        //使用工具类里边的方法避免了直接new,降低了依赖,解耦
        List<Integer> categoryListId = Lists.newArrayList();
        Set<Category> categorySet = Sets.newHashSet();
        //类中非静态方法的调用
        findChildCategory(categorySet, categoryId);
        for (Category category : categorySet) {
            categoryListId.add(category.getId());
        }
        return ServerResponse.createBySuccess(categoryListId);
    }

    // 递归算法,算出子节点(就是自己调用自己)
    //todo:算法优化
    //重写Category类的hashcode和equals方法
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        //查找子节点,递归算法一定要有一个退出的条件(退出条件是子节点是否为空)
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectByPrimaryKeyId(categoryId);
        //进入了递归
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }

        return categorySet;

    }
}


