package com.howie.quanmall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.howie.common.utils.HttpUtils;
import com.howie.quanmall.thirdparty.component.SmsComponent;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class QuanmallThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;
    @Autowired
    SmsComponent smsComponent;

    @Test
    void contextLoads() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("C:\\Users\\82398\\Desktop\\aaaa06049fb474e3.jpg");
        ossClient.putObject("quanmall", "aaaa06049fb474e3.jpg", inputStream);
        ossClient.shutdown();
        System.out.println("上传完成！");
    }

    @Test
    public void sendSms(){
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "c682a9cf84fa457099ed38e2237858b8";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "18815015332");
        querys.put("param", "**code**:12345,**minute**:5");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCode(){
        smsComponent.sendSmsCode("18815015332","435225");
    }
}
