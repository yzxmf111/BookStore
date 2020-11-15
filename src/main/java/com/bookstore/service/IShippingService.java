package com.bookstore.service;

import com.github.pagehelper.PageInfo;
import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Shipping;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/29 22:57
 */

public interface IShippingService {

    public ServerResponse add(Integer userId , Shipping shipping);

    public ServerResponse del(Integer userId, Integer shippingId);

    public ServerResponse update(Integer userId, Shipping shipping);

    public ServerResponse select(Integer userId, Integer shippingId);

    public ServerResponse<PageInfo> getShippingList(Integer userId, Integer pageNum, Integer pageSize);
}
