package com.howie.quanmall.cart.service;


import com.howie.quanmall.cart.vo.Cart;
import com.howie.quanmall.cart.vo.CartItem;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/9 13:19
 * @Version 1.0
 */
public interface CartService{
    CartItem addToCart(Long skuId, Integer num) throws Exception;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws Exception;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeCountItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
