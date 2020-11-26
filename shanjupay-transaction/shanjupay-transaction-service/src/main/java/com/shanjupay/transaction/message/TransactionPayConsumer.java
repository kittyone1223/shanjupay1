package com.shanjupay.transaction.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.transaction.api.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-26 12:16
 **/

@Slf4j
@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_RESULT", consumerGroup = "CID_ORDER_CONSUMER")
public class TransactionPayConsumer implements RocketMQListener<MessageExt> {

    @Resource
    TransactionService transactionService;


    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);

        // 接收到消息  内容包括订单状态
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(jsonString, PaymentResponseDTO.class);
        String tradeNo = paymentResponseDTO.getTradeNo();  //支付宝微信的订单号
        String outTradeNo = paymentResponseDTO.getOutTradeNo();// 闪聚平台信的订单号
        TradeStatus tradeState = paymentResponseDTO.getTradeState(); //订单状态
        switch (tradeState) {
            case SUCCESS:
                // 支付成功时  修改订单状态为支付成功
                transactionService.updateOrderTradeNoAndTradeState(tradeNo, outTradeNo, "2");
            case REVOKED:
                // 支付关闭时 修改订单状态为关闭
                transactionService.updateOrderTradeNoAndTradeState(tradeNo, outTradeNo, "4");
            case FAILED:
                // 支付失败时  修改订单状态为失败
                transactionService.updateOrderTradeNoAndTradeState(tradeNo, outTradeNo, "5");
            default:
                throw new RuntimeException(String.format("无法解析支付结果:%s", body));
        }
    }
}
