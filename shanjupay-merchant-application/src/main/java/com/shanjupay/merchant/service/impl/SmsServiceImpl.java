package com.shanjupay.merchant.service.impl;


import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.effectiveTime}")
    private String effectiveTime;



    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sendSms(String phone){
//        "curl -X POST \"http://localhost:56085/sailing/generate?effectiveTime=600&name=sms\" -H \"accept: */*\" -H \"Content-Type: application/json\" -d \"{\\\"mobile\\\":123123}\""
        String url = smsUrl + "/generate?effectiveTime=" + effectiveTime+"&name=sms";// 验证码过期时间为600秒
        log.info("调用短信微服务发送验证码：url:{}", url);

        // 请求体
        HashMap<String, Object> body = new HashMap<>();
        body.put("mobile", phone);
        // 请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(body, httpHeaders);

        Map responseBody=null;
        try{
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            responseBody = exchange.getBody();
        }catch (Exception e){
            log.info(e.getMessage(),e);
            throw new RuntimeException("发送验证码出错");
        }

        if (responseBody == null || responseBody.get("result")==null){
            throw new RuntimeException("发送验证码出错");
        }

        Map resultMap = (Map)responseBody.get("result");
        return resultMap.get("key").toString();
    }

    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) {
//        curl -X POST "http://localhost:56085/sailing/verify?name=sms&verificationCode=629849&verificationKey=sms%3A55af4d023fe64c56bf2754251d92d0b9" -H "accept: */*"
        String url = smsUrl+"/verify?name=sms&verificationCode="+verifiyCode+"&verificationKey="+verifiyKey;

        Map responseMap =null;
        try{
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            responseMap = exchange.getBody();
            log.info("校验验证码，响应内容：{}", JSON.toJSONString(responseMap));
        }catch (Exception e){
            e.printStackTrace();
            log.info(e.getMessage(),e);
            throw new BusinessException(CommonErrorCode.E_100102);
            //throw new RuntimeException("验证码错误");
        }

        if (responseMap==null||responseMap.get("result")==null||!(Boolean) responseMap.get("result")){
            throw new BusinessException(CommonErrorCode.E_100102);
            //throw new RuntimeException("验证码错误");
        }

    }
}
