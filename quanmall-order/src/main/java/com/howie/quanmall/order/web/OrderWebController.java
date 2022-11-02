package com.howie.quanmall.order.web;

import com.howie.quanmall.order.entity.OrderEntity;
import com.howie.quanmall.order.service.OrderService;
import com.howie.quanmall.order.vo.OrderConfirmVo;
import com.howie.quanmall.order.vo.OrderSubmitVo;
import com.howie.quanmall.order.vo.SubmitOrderRespVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.UUID;


/**
 * @Author Howie
 * @Date 2022/2/10 19:14
 * @Version 1.0
 */
@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Transactional
    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    @PostMapping("submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderRespVo respVo = orderService.submitOrder(vo);
        if (respVo.getCode()==0){
            //下单成功来到订单支付选择页
            model.addAttribute("submitOrder",respVo);
            return "pay";
        }else {
            //下单失败回到订单确认页
            String msg = "下单失败；";
            switch (respVo.getCode()){
                case 1: msg += "订单信息过期，请刷新再次提交！"; break;
                case 2: msg += "无法选购无货商品，请修改后再次提交！"; break;
                case 3: msg += "库存锁定失败，商品库存不足"; break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.quanmall.com/toTrade";
        }
    }

   /* @ResponseBody
    @GetMapping("/test")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        System.out.println(orderEntity.getOrderSn());
        return "ok";
    }*/
}
