package com.shanjupay.test.mq.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shanjupay.test.mq.model.OrderExt;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 13:42
 **/

@Component
public class ProducerSimple {

    //@Autowired
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送同步消息
     * @param topic
     * @param msg
     */
    public void  sendSyncMsg(String topic,String msg){
        SendResult sendResult = rocketMQTemplate.syncSend(topic, msg);
    }

    public void  sendAsyncMsg(String topic,String msg){
        rocketMQTemplate.asyncSend(topic, msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                //成功回调
                System.out.println(sendResult.getSendStatus());
            }

            @Override
            public void onException(Throwable throwable) {
                //回调失败
                System.out.println(throwable.getMessage());
            }
        });
    }

    /**
     * 消息额呢绒为json格式
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJson(String topic, OrderExt orderExt){


        // 发送同步消息   消息内容指定为将orderext转为json
        rocketMQTemplate.convertAndSend(topic,orderExt);
        System.out.printf("send msg : %s",orderExt);
    }


    /**
     * 同步延迟方法
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJsonDelay(String topic, OrderExt orderExt){

        //发送同步消息 消息内容为orderExt转为json
        Message<OrderExt> message = MessageBuilder.withPayload(orderExt).build();
        // 指定发送消息（毫秒）和延迟等级
        rocketMQTemplate.syncSend(topic,message,1000,3);
        System.out.printf("send msg : %s",orderExt);
    }



    public void sendAsyncMsgByJsonDelay(String topic,OrderExt orderExt) throws JsonProcessingException, RemotingException, MQClientException, InterruptedException {

        // 消息内容将orderExt转为json
        String json = rocketMQTemplate.getObjectMapper().writeValueAsString(orderExt);


        org.apache.rocketmq.common.message.Message message = new org.apache.rocketmq.common.message.Message(topic,json.getBytes(Charset.forName("utf-8")));


        // 设置延迟等级
        message.setDelayTimeLevel(3);

        // 发送异步消息
        rocketMQTemplate.getProducer().send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println(sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }
        });

        System.out.printf("send msg: %s",orderExt);
    }

}
