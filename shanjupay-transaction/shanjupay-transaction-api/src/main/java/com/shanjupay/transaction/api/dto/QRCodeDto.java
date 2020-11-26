package com.shanjupay.transaction.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: shanjupay
 * @author: Mr.Wang
 * @create: 2020-11-18 18:29
 **/

@Data
@NoArgsConstructor
public class QRCodeDto  implements Serializable {
    private Long merchantId;
    private String appId;
    private Long storeId;
    private String subject;  //商品标题
    private String body;   // 订单描述
}
