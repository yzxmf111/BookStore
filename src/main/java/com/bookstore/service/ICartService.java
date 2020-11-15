package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.vo.CartVo;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/27 17:18
 */


public interface ICartService {

    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    public ServerResponse<CartVo> delete(Integer userId,String productIds);

    public ServerResponse<CartVo> update(Integer userId,Integer productId, Integer count);

    public ServerResponse<CartVo> list(Integer userId);

    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked);

    public ServerResponse<Integer> getCartProductCount(Integer userId);
}
