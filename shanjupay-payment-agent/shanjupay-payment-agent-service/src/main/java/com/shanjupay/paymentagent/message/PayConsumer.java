package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-26 11:05
 **/

@Slf4j
@Service
@RocketMQMessageListener(topic = "TP_PAYMENT_ORDER", consumerGroup = "CID_PAYMENT_CONSUMER")
public class PayConsumer implements RocketMQListener<MessageExt> {


    @Resource
    private PayChannelAgentService payChannelAgentService;


    @Autowired
    private PayProducer payProducer;

    @Override
    public void onMessage(MessageExt messageExt) {

        // 取出消息内容
        String body = new String(messageExt.getBody());
        PaymentResponseDTO response = JSON.parseObject(body, PaymentResponseDTO.class);
        String outTradeNo = response.getOutTradeNo();
        String msg = response.getMsg();
        String param = String.valueOf(response.getContent());
        AliConfigParam aliConfigParam = JSON.parseObject(param, AliConfigParam.class);

        // 判断是支付宝还是微信
        if ("ALIPAY_WAP".equals(msg)) {
            // 查询支付宝支付结果
            payChannelAgentService.queryPayOrderByAli(aliConfigParam,outTradeNo);
        }else if ("WX_JSAPI".equals(msg)){
            //  微信操作
        }


        //返回查询获得的支付状态
        if (TradeStatus.UNKNOWN.equals(response.getTradeState())||TradeStatus.USERPAYING.equals(response.getTradeState())){
            //再支付状态未知或支付中 抛出异常会重新更新此消息
            log.info("支付代理‐‐‐支付状态未知，等待重试");
            throw new RuntimeException("支付状态未知，等待重试");
        }

        // 将订单状态再次发到mq

        payProducer.payResultNotice(response);
    }
}
