package com.bookstore.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.CartMapper;
import com.bookstore.dao.ProductMapper;
import com.bookstore.pojo.Cart;
import com.bookstore.pojo.Product;
import com.bookstore.service.ICartService;
import com.bookstore.utils.BigDecimalUtil;
import com.bookstore.utils.PropertiesUtil;
import com.bookstore.vo.CartProductVo;
import com.bookstore.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/27 17:18
 */

@Service("iCartServiceImpl")
public class ICartServiceImpl implements ICartService {

    @Autowired
    private Cart cart;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartProductVo cartProductVo;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        //这里的userId不为null
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cartItem = cartMapper.selectByUserIdProductId(userId, productId);
        if (cartItem == null) {
            //Cart cartItem = new Cart();
            //购物车里便没有该产品,加上该产品
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setProductId(productId);
            //这个要设置的,不然后边无法判断是否全选
            cart.setChecked(Const.Cart.CHECKED);
            int resultCount = cartMapper.insertSelective(cart);
        } else {
            //更改产品数量
            cartItem.setQuantity(cartItem.getQuantity() + count);
            cartMapper.updateByPrimaryKeySelective(cartItem);
        }
//        CartVo cartVo = this.getCartVo(userId);
//        return ServerResponse.createBySuccess(cartVo);
        return list(userId);
    }

    //返回删除一些产品后的CartVo
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (productIds == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //防止横向越权,只能删除自己购物车的商品
        int resultCount = cartMapper.deleteByUserIdAndProductIds(userId, productList);
        if (resultCount > 0) {
//            CartVo cartVo = this.getCartVo(userId);
//            return ServerResponse.createBySuccess(cartVo);
            return list(userId);
        }
        return ServerResponse.createByErrorMessage("删除失败");
    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
          /*  CartVo cartVo = this.getCartVo(userId);
            return ServerResponse.createBySuccess(cartVo);*/
            return list(userId);
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
    }

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVo(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
       /** if (productId == null || checked == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setChecked(checked);
            cartMapper.updateByPrimaryKeySelective(cart);//更新
            return list(userId);//显示给前端观看
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        现在我们使用一个高复用的service和dao接口实现4个方法
        */

       //todo 在选中或不选中的同时 计算总价(优惠)
       cartMapper.updateCheckedOrUnChecked(userId,productId,checked);
        CartVo cartVo = this.getCartVo(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        int resultCount = cartMapper.selectProductCountByUserId(userId);
        return ServerResponse.createBySuccess(resultCount);
    }


    private CartVo getCartVo(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                //CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        //库存充足的时候
                        buyLimitCount = cartItem.getQuantity();
                        //和前端约定好了,传了这个会进行相应的页面显示
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存,前端也会进行相应的更改
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }

}
