package com.howie.quanmall.order.interceptor;

import com.howie.common.constant.AuthServerConstant;
import com.howie.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Howie
 * @Date 2022/2/10 19:19
 * @Version 1.0
 */

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", requestURI);
        boolean match1 = antPathMatcher.match("/payed/**", requestURI);
        if (match || match1){
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            loginUser.set(attribute);
            return true;
        }else {
            request.getSession().setAttribute("msg","请先登录！");
            response.sendRedirect("http://auth.quanmall.com/login.html");
            return false;
        }
    }
}
