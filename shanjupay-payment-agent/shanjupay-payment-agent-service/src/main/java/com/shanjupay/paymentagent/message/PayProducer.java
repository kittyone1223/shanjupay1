package com.shanjupay.paymentagent.message;

import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-26 10:58
 **/

@Slf4j
@Component
public class PayProducer {
    // 消息topic  订单结果查询
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";


    @Resource
    private RocketMQTemplate rocketMQTemplate;


    public void payOrderNotice(PaymentResponseDTO result) {
        log.info("支付通知发送延迟消息:{}", result);

        try {
            // 处理消息储存格式
            Message<PaymentResponseDTO> message = MessageBuilder.withPayload(result).build();
            rocketMQTemplate.syncSend(TOPIC_ORDER, message, 1000, 3);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage(), e);
        }
    }

    // 订单结果  主题
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";


    //发送消息（支付结果）
    public void payResultNotice(PaymentResponseDTO paymentResponseDTO){
        rocketMQTemplate.convertAndSend(TOPIC_RESULT,paymentResponseDTO);
    }
}
