package com.howie.quanmall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.howie.common.constant.AuthServerConstant;
import com.howie.common.utils.HttpUtils;
import com.howie.common.utils.R;
import com.howie.quanmall.authserver.feign.MemberFeignService;
import com.howie.common.vo.MemberRespVo;
import com.howie.quanmall.authserver.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * 处理社交登录请求
 * @Author Howie
 * @Date 2022/2/8 13:23
 * @Version 1.0
 */
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //根据code换取access token
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id","1256713968");
        map.put("client_secret","f0d093906d3f120672cc880d6a8c9c38");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.quanmall.com/oauth2/weibo/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String,String>(), null, map);

        //处理
        if (response.getStatusLine().getStatusCode()==200){
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0){
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                //修改domain作用域
                //使用json序列化
                session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                //System.out.println(data);
                //登录成功就跳回首页
                return "redirect:http://quanmall.com";
            }else {
                return "redirect:http://auth.quanmall.com/login.html";
            }
        }else {
            return "redirect:http://auth.quanmall.com/login.html";
        }
    }
}
