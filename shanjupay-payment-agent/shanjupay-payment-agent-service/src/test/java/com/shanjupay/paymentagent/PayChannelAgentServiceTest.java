package com.shanjupay.paymentagent;

import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 19:32
 **/


@SpringBootTest
@RunWith(SpringRunner.class)
public class PayChannelAgentServiceTest {

    @Resource
    PayChannelAgentService payChannelAgentService;

    @Test
    public void testqueryPayOrderByAli() {
        //应用id
        String APP_ID = "2016101500688360";
        //应用私钥
        String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC783ED6z/d7FAXjb+NGQG/kjsDTAjvbXpquI9+TqWICCHhRe6CZvQ1WNhPOA0PP9yzrNKeIhAE+/bfhm5IeJdtWUZsQm5vbHx/93r9jHFGxmVoG8ogx+s92CPkqbiLoPjmnlytuAN3Hjtk1zpjA3zr+pw38cCTUCXTd/QcBY4d6p4MBh1EJJUx7JapTeid4kIB4o67PW7C6EHfpejV20BvPDrKQseH3nchdsOvtLZ9D0s17xQKuNLNqZGKqcfkqrlnFzLmxY801glviM1njwWbPo9cGJp1zOVqbZr/819XQvWFgQY2gbHr2cQXshmYa82WInWA9jSAl7S1HvW8rofbAgMBAAECggEBAImkl+CR2RzyIhmNwnlXlfMw6SzZ8zU8zMj5ToTmnc1If19V8nznXvLulMM3PkEruLqQ11HdijPMPpil/3/taKA5IBzKbcwF/AtPN3AV+SMmyj77EwT8w+dvsaGVNcRz57Qkx6SlPUxwBHkuMeEvPb4ns145GpoHuRvsHgajfteiqEDxFiSuxRwe343B/K5GWB0LQVqdnnG/7vavFErsrKp1KGqsUUp4Rn7qEArCkT7vsIFsOF81+wT7Qxx5pBXDUxgxt871v9r1zC4tv1Hnoo7YezB+IqJPITbcpBdNu1vgcrp/m2wkU0/ahBALMMaIyg0YKTePRVQhfKFTfPv3OwECgYEA3CtOEp9lzfNWJBwvWyudC1a8qBvyqxOY+x+q+BdEB1QE3n6PjvzxDvizK/XNH6GH3eFz0QKCYYLQ8bm4niseBN5x84wyN3FqTo6e7nMj0TVZmGfeizk9mAp4Pf6BqZS4NswInORjPJz/4fOKNdCQItftv6U65TmlK4+dpTGgcoECgYEA2oncLEJJlUxsg6CCruujNeoWAlO/kicACEVSZ9hKIRTvEi1IRs5/1/j4K0dMJikSzJpz8eFHWuahr0qLnQk245eHLioWaHs3wtuyiNSPR9fDFWfDLrWf0udJCqaTtcMTjliuCI4A/yqwMbLTSNqhXqk4o8nhwySf89CKfhpT1FsCgYAmq7iHztV7ygzMROMQeWWZE0AJqvcBk2ygVxyZfMSnINvGFKa0b7sLbSVTSFYiAyYxE6NbB7zM0cRPYNaLeXlJU6TiKTK8yD/q0mQZ78FPwrYc7cmJ3KDz7orWILi3h3afsVCl9ft0LwNGtZcGAr7+T7zjl2rzzh8zuoLu+/t5gQKBgDc2yYftH4Y3DtuavCnRoyTGBdWEr0wPynRMf2t5PBFrppNjGXW6SpaYcqsMknK5weQryct87XhL+OG+5dNBNrr918uUGqE/MHFs7Iihm6m9G67WznuIq96vTMcCGyO7K7+1LBNBPYM6l9WWpscrZcXy3zqSkRWqtTb2o2cE49UtAoGBAJkgBjH3NbWq/mb+xhAz1k3wWZYA7XLLqrxC9hbcujUgApF1jEFBYeIlju5dVx5gHY05GnP6B7/Of+XszAmT1tKegmmG75MJBm7Hq0unSRAT/bQddIiGTpy2RrWFX9bn00xMApGVPY5SvbV4m/pYpK0UwYXy3VryGqftCEoL0TQO";
        //支付宝公钥
        String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj00crrOGcmFheRRHIRuUAobSpBuxs1ed58SetlQ1/KV9TWitLwcQa9gmEK0/LMa4K5/M2P3TKzUB9myCxpBjR8bkfxf/OyMlfm7/uSThHqhYGYcrKkf2nuhdcn4BXVzUcj4cEQXie3+TdTj0nhv6pxmGp+zmut2MpIoBHKz1egjZZzuLz00UIrAhZ9YWJ2nSsU4m0bmXtMNNrxhThTwTDAz518pRt53F5MWEbMkADRu64JuMWF5+hAB0+XXuzDTAwVrnAcZwjpIlBaEzHIR18ULVBEL7omwNmDJJw4rrjw2EqnFFbe3IfNPCscXwtpmTT25jabHTb47lTLKm/ir6JQIDAQAB";
        String CHARSET = "utf-8";
        String serverUrl = "https://openapi.alipaydev.com/gateway.do";
        AliConfigParam aliConfigParam = new AliConfigParam();
        aliConfigParam.setUrl(serverUrl);
        aliConfigParam.setCharest(CHARSET);
        aliConfigParam.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        aliConfigParam.setRsaPrivateKey(APP_PRIVATE_KEY);
        aliConfigParam.setAppId(APP_ID);
        aliConfigParam.setFormat("json");
        aliConfigParam.setSigntype("RSA2");

        //AliConfigParam aliConfigParam,String outTradeNo
        PaymentResponseDTO paymentResponseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, "SJ1331430476053348352");
        System.out.println(paymentResponseDTO);
    }
}