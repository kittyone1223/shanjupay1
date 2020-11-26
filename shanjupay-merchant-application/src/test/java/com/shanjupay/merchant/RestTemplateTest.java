package com.shanjupay.merchant;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RestTemplateTest {
    @Autowired
    RestTemplate restTemplate;


    @Test
    public void getHtml() {
        String url = "http://www.baidu.com/";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        System.out.println(body);
    }

    // 获取验证码测试方法
    @Test
    public void testGetSmsCode(){
        String url = "http://localhost:56085/sailing/generate?name=sms&effectiveTime=600";//验证码过 期时间为600秒
        String phone="17369569915";
        log.info("调用短信微服务发送验证码：url:{}", url);
        // 请求体
        HashMap<String, Object> body = new HashMap<>();
        body.put("mobile",phone);
        // 请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body,httpHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        log.info("请求验证码服务得到响应：{}", JSON.toJSONString(exchange));
        Map responseMap = exchange.getBody();
        System.out.println(responseMap);
        Map result = (Map)responseMap.get("result");
        Object key = result.get("key");
        System.out.println("************************************************"+key);

    }
}
