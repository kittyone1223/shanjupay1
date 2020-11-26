package com.shanjupay.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支付宝对接测试接口
 *
 * @program: shanjupay
 * @author: Mr.Wang
 * @create: 2020-11-18 11:56
 **/

@Controller
public class PayTestController {

    String APP_ID = "2016101500688360";
    String serverUrl = "https://openapi.alipaydev.com/gateway.do";
    String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC783ED6z/d7FAXjb+NGQG/kjsDTAjvbXpquI9+TqWICCHhRe6CZvQ1WNhPOA0PP9yzrNKeIhAE+/bfhm5IeJdtWUZsQm5vbHx/93r9jHFGxmVoG8ogx+s92CPkqbiLoPjmnlytuAN3Hjtk1zpjA3zr+pw38cCTUCXTd/QcBY4d6p4MBh1EJJUx7JapTeid4kIB4o67PW7C6EHfpejV20BvPDrKQseH3nchdsOvtLZ9D0s17xQKuNLNqZGKqcfkqrlnFzLmxY801glviM1njwWbPo9cGJp1zOVqbZr/819XQvWFgQY2gbHr2cQXshmYa82WInWA9jSAl7S1HvW8rofbAgMBAAECggEBAImkl+CR2RzyIhmNwnlXlfMw6SzZ8zU8zMj5ToTmnc1If19V8nznXvLulMM3PkEruLqQ11HdijPMPpil/3/taKA5IBzKbcwF/AtPN3AV+SMmyj77EwT8w+dvsaGVNcRz57Qkx6SlPUxwBHkuMeEvPb4ns145GpoHuRvsHgajfteiqEDxFiSuxRwe343B/K5GWB0LQVqdnnG/7vavFErsrKp1KGqsUUp4Rn7qEArCkT7vsIFsOF81+wT7Qxx5pBXDUxgxt871v9r1zC4tv1Hnoo7YezB+IqJPITbcpBdNu1vgcrp/m2wkU0/ahBALMMaIyg0YKTePRVQhfKFTfPv3OwECgYEA3CtOEp9lzfNWJBwvWyudC1a8qBvyqxOY+x+q+BdEB1QE3n6PjvzxDvizK/XNH6GH3eFz0QKCYYLQ8bm4niseBN5x84wyN3FqTo6e7nMj0TVZmGfeizk9mAp4Pf6BqZS4NswInORjPJz/4fOKNdCQItftv6U65TmlK4+dpTGgcoECgYEA2oncLEJJlUxsg6CCruujNeoWAlO/kicACEVSZ9hKIRTvEi1IRs5/1/j4K0dMJikSzJpz8eFHWuahr0qLnQk245eHLioWaHs3wtuyiNSPR9fDFWfDLrWf0udJCqaTtcMTjliuCI4A/yqwMbLTSNqhXqk4o8nhwySf89CKfhpT1FsCgYAmq7iHztV7ygzMROMQeWWZE0AJqvcBk2ygVxyZfMSnINvGFKa0b7sLbSVTSFYiAyYxE6NbB7zM0cRPYNaLeXlJU6TiKTK8yD/q0mQZ78FPwrYc7cmJ3KDz7orWILi3h3afsVCl9ft0LwNGtZcGAr7+T7zjl2rzzh8zuoLu+/t5gQKBgDc2yYftH4Y3DtuavCnRoyTGBdWEr0wPynRMf2t5PBFrppNjGXW6SpaYcqsMknK5weQryct87XhL+OG+5dNBNrr918uUGqE/MHFs7Iihm6m9G67WznuIq96vTMcCGyO7K7+1LBNBPYM6l9WWpscrZcXy3zqSkRWqtTb2o2cE49UtAoGBAJkgBjH3NbWq/mb+xhAz1k3wWZYA7XLLqrxC9hbcujUgApF1jEFBYeIlju5dVx5gHY05GnP6B7/Of+XszAmT1tKegmmG75MJBm7Hq0unSRAT/bQddIiGTpy2RrWFX9bn00xMApGVPY5SvbV4m/pYpK0UwYXy3VryGqftCEoL0TQO";
    String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj00crrOGcmFheRRHIRuUAobSpBuxs1ed58SetlQ1/KV9TWitLwcQa9gmEK0/LMa4K5/M2P3TKzUB9myCxpBjR8bkfxf/OyMlfm7/uSThHqhYGYcrKkf2nuhdcn4BXVzUcj4cEQXie3+TdTj0nhv6pxmGp+zmut2MpIoBHKz1egjZZzuLz00UIrAhZ9YWJ2nSsU4m0bmXtMNNrxhThTwTDAz518pRt53F5MWEbMkADRu64JuMWF5+hAB0+XXuzDTAwVrnAcZwjpIlBaEzHIR18ULVBEL7omwNmDJJw4rrjw2EqnFFbe3IfNPCscXwtpmTT25jabHTb47lTLKm/ir6JQIDAQAB";
    String CHARSET = "utf-8";

    @GetMapping("/alipaytest")
    public void alipaytest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的 request

        alipayRequest.setBizContent("{" + " \"out_trade_no\":\"20150320010101123\"," + " \"total_amount\":\"0.01\"," + " \"subject\":\"Iphone6 16G\"," + " \"product_code\":\"QUICK_WAP_PAY\"" + " }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
}
