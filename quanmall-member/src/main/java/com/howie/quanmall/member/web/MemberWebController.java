package com.howie.quanmall.member.web;

import com.howie.common.utils.R;
import com.howie.quanmall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Howie
 * @Date 2022/2/12 21:18
 * @Version 1.0
 */

@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberList.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, Model model){
        Map<String,Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders",r);
        return "list";
    }
}
