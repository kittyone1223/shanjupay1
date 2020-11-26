package com.shanjupay.test.mq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-25 15:29
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderExt implements Serializable {



    private String id;
    private Date createTime;
    private Long money;
    private String title;


}
