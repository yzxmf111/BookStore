package com.bookstore.service;

import com.github.pagehelper.PageInfo;
import com.bookstore.common.ServerResponse;
import com.bookstore.vo.OrderVo;

import java.util.Map;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/31 11:40
 */
public interface IOrderService {

    public ServerResponse<OrderVo> createOrder(Integer userId, Integer shippingId);

    public ServerResponse cancelOrder(Integer userId,Long orderNo);

    public ServerResponse getOrderProductMsg(Integer userId);

    public ServerResponse getOrderDetail(Integer userId, Long orderNo);

    public ServerResponse<PageInfo> getOrderVoList(Integer userId, Integer pageNum, Integer pageSize);

    //后台
    public ServerResponse<PageInfo> getManageOrderList(Integer pageNum, Integer pageSize);

    public ServerResponse<OrderVo> getOrderDetail(Long orderNo);

    public ServerResponse<PageInfo> search(Long orderNo,Integer pageNum,Integer pageSize);

    public ServerResponse sendGoods(Long orderNo);

    //支付前台
    public ServerResponse pay(Integer userId, Long orderNo, String realPath);

    public ServerResponse queryOrderStatue(Integer userId, Long orderNo);

    public ServerResponse aliCallback(Map<String,String> params);
}
