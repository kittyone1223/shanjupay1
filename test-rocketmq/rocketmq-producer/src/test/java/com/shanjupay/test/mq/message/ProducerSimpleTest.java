package com.shanjupay.test.mq.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shanjupay.test.mq.model.OrderExt;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 13:46
 **/

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerSimpleTest {

    //    @Autowired
    @Resource
    private RocketMQTemplate rocketMQTemplate;


    @Autowired
    private ProducerSimple producerSimple;

    /**
     * 测试发送消息
     */
    @Test
    public void testSendSyncMsg() {
        //rocketMQTemplate.syncSend("my-topic","第一条消息");
        producerSimple.sendSyncMsg("my-topic", "第二条消息");
        System.out.println("end------");
    }

    @Test
    public void testSendASyncMsg() {
        producerSimple.sendAsyncMsg("my-topic", "第一条异步消息");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSendSyncMsgObj() {
        producerSimple.sendMsgByJson("my-topic-obj", new OrderExt("1", new Date(), 1000L, "123"));
    }


    /// 测试发送同步消息
    @Test
    public void testSendMsgByJsonDelay() throws JsonProcessingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
        OrderExt orderExt = new OrderExt();
        orderExt.setId(UUID.randomUUID().toString());
        orderExt.setCreateTime(new Date());
        orderExt.setMoney(168L);
        orderExt.setTitle("测试订单");
        this.producerSimple.sendMsgByJsonDelay("my-topic", orderExt);
        System.out.println("end...");
    }


    //测试发送异步消息
    @Test
    public void testSendAsyncMsgByJsonDelay() throws JsonProcessingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
        OrderExt orderExt = new OrderExt();
        orderExt.setId(UUID.randomUUID().toString());
        orderExt.setCreateTime(new Date());
        orderExt.setMoney(168L);
        orderExt.setTitle("测试订单");
        this.producerSimple.sendAsyncMsgByJsonDelay("my-topic", orderExt);
        System.out.println("end...");
        Thread.sleep(20000);
    }
}
