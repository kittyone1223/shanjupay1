package com.shanjupay.test.mq.message;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 *
 * 消息消费者监听类
 *
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 13:50
 **/

@Component
@RocketMQMessageListener(topic = "my-topic",consumerGroup = "demo-consumer-group")
public class ConsumerSimple implements RocketMQListener<String> {


    @Override
    public void onMessage(String s) {
        System.out.println(s);
    }
}
