package com.howie.quanmall.cart.controller;

import com.howie.quanmall.cart.interceptor.CartInterceptor;
import com.howie.quanmall.cart.service.CartService;
import com.howie.quanmall.cart.vo.Cart;
import com.howie.quanmall.cart.vo.CartItem;
import com.howie.quanmall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/9 13:22
 * @Version 1.0
 */
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    // TODO: 2022/2/10  ajax获取购物车订单项，有购物车的都要远程调用
    //获取购物车订单项
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws Exception {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) throws Exception {
        cartService.addToCart(skuId,num);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.quanmall.com/addToCart.html";
    }

    @GetMapping("/addToCart.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check")Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.quanmall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num")Integer num){
        cartService.changeCountItem(skuId,num);
        return "redirect:http://cart.quanmall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.quanmall.com/cart.html";
    }
}
