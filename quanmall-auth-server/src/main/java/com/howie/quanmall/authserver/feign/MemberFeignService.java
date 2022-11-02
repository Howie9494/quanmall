package com.howie.quanmall.authserver.feign;

import com.howie.common.utils.R;
import com.howie.quanmall.authserver.vo.SocialUser;
import com.howie.quanmall.authserver.vo.UserLoginVo;
import com.howie.quanmall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Howie
 * @Date 2022/2/7 17:20
 * @Version 1.0
 */
@FeignClient("quanmall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser) throws Exception;
}
