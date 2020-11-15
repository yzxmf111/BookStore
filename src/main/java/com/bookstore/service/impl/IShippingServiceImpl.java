package com.bookstore.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.ShippingMapper;
import com.bookstore.pojo.Shipping;
import com.bookstore.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/29 22:57
 */

@Service("iShippingService")
public class IShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int count = shippingMapper.insert(shipping);
        if (count > 0) {
            HashMap map = Maps.newHashMap();
            map.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", map);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse del(Integer userId, Integer shippingId) {

        if (shippingId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        int count = shippingMapper.deleteByUserIdAndShippingId(userId, shippingId);
        if (count > 0) {
            return ServerResponse.createBySuccess("删除地址成功 ");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }
    //只要是从当前电脑的session获取的userId 那么就和大程度上是安全的
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int count = shippingMapper.updateByUserIdAndShippingId( shipping);
        if (count > 0) {
            return ServerResponse.createBySuccess("更新地址成功 ");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(userId, shippingId);
        if (shipping != null) {
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByErrorMessage("查询地址失败");
    }

    public ServerResponse<PageInfo> getShippingList(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.getShippingList(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}

