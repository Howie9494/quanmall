package com.howie.quanmall.thirdparty.controller;

import com.howie.common.utils.R;
import com.howie.quanmall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Howie
 * @Date 2022/2/7 13:31
 * @Version 1.0
 */

@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
