package com.howie.quanmall.search;

import com.alibaba.fastjson.JSON;
import com.howie.quanmall.search.config.QuanmallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class QuanmallSearchApplicationTests {

    @Autowired
    //@Qualifier("esRestClient")
    private RestHighLevelClient client;

    @Test
    void searchData() throws IOException{
        //创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        sourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        sourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));
        System.out.println(sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        //执行检索
        SearchResponse response = client.search(searchRequest, QuanmallElasticSearchConfig.COMMON_OPTIONS);
        //分析结果
        System.out.println(response.toString());
    }

    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        //users.source("userName","zhangsan","age","18","gender","M");
        User user = new User();
        user.setUserName("howie");
        user.setAge(18);
        user.setGender("男");
        String string = JSON.toJSONString(user);
        indexRequest.source(string,XContentType.JSON);

        //执行操作
        IndexResponse index = client.index(indexRequest, QuanmallElasticSearchConfig.COMMON_OPTIONS);

        //提供有用的相应数据
        System.out.println(index);
    }

    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
