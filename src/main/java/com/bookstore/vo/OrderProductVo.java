package com.bookstore.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: Tian
 * @time: 2020/8/2 22:19
 */


public class OrderProductVo implements Serializable {

    private List<OrderItemVo> orderItemVoList;

    private String imageHost;

    private BigDecimal productTotalPrice;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }
}
