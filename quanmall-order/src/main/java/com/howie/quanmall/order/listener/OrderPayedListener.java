package com.howie.quanmall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.howie.quanmall.order.config.AlipayTemplate;
import com.howie.quanmall.order.service.OrderService;
import com.howie.quanmall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author Howie
 * @Date 2022/2/12 22:31
 * @Version 1.0
 */

@RestController
@Slf4j
public class OrderPayedListener {
    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping("/payed/nodify")
    public String handleAlipayed(PayAsyncVo vo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //支付宝会进行最大努力通知
        //只要收到异步通知，就需要返回success，支付宝就不再通知
        //验签
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified){
            log.info("签名验证成功");
            String result = orderService.handlePayResult(vo);
            return result;
        }else {
            log.error("签名验证失败");
            return "failed";
        }

    }
}
