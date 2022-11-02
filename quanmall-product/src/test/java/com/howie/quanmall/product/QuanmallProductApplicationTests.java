package com.howie.quanmall.product;

import com.howie.quanmall.product.dao.AttrGroupDao;
import com.howie.quanmall.product.dao.SkuSaleAttrValueDao;
import com.howie.quanmall.product.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@Slf4j
class QuanmallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test2(){
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(4L));
    }

    @Test
    public void test1(){
        System.out.println(attrGroupDao.getAttrGroupWithAttrsBySpuId(4L, 225L));
    }

    @Test
    public void test(){
        System.out.println(redissonClient);
    }

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","world");
        System.out.println(ops.get("hello"));
    }

    @Test
    public void testUpload() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
  /*      String endpoint = "oss-cn-hangzhou.aliyuncs.com";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tSV5Zyf2Lu4QjM8K4h8";
        String accessKeySecret = "4E0TYS09PxsojhdfUxSCCHQYQ3LarC";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);*/

// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\82398\\Desktop\\aaaa06049fb474e3.jpg");
/*// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("quanmall", "aaaa06049fb474e3.jpg", inputStream);

// 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成！");*/
    }

    @Test
    void contextLoads() {
        log.info(brandService.getById(1).toString());

    }

}
