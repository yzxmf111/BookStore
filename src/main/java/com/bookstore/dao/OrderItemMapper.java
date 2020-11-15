package com.bookstore.dao;

import com.bookstore.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    void batchInsert(List<OrderItem> orderItemList);

    List<OrderItem> selectByOrderNoAndUserId(@Param("userId")Integer userId, @Param("orderNo")Long orderNo);

    List<OrderItem> selectByOrderNo(@Param("orderNo")Long orderNo);
}