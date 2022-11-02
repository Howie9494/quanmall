package com.howie.quanmall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/9 13:05
 * @Version 1.0
 */
public class Cart {

    List<CartItem> items;
    private Integer count; //商品总数量
    private Integer countType;//商品类型总数量
    private BigDecimal totalAmount;//商品总价
    private BigDecimal reduce = new BigDecimal("0");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCount() {
        int count = 0;
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int countType = 0;
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                countType += 1;
            }
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                if (item.getCheck()){
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }
}
