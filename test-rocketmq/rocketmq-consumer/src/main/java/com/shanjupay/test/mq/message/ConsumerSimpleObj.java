package com.shanjupay.test.mq.message;

import com.shanjupay.test.mq.model.OrderExt;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 15:38
 **/

@Component
@RocketMQMessageListener(topic = "my-topic-obj",consumerGroup = "demo-consumer-group-obj")
public class ConsumerSimpleObj implements RocketMQListener<MessageExt> {



    // 接受到消息调用此方法
    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);

        System.out.println(jsonString);

        int reconsumeTimes = messageExt.getReconsumeTimes();
        if (reconsumeTimes>2){
            // 将此消息加入数据库  单独处理

        }
        if (1==1){
            throw new RuntimeException("数据处理出错");
        }
    }
}
