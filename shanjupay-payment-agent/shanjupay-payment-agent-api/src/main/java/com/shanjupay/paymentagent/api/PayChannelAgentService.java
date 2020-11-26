package com.shanjupay.paymentagent.api;


import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;

public interface PayChannelAgentService {

    PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean);


    /**
     * 支付宝交易状态
     * @param aliConfigParam  支付宝渠道
     * @param outTradeNo  shanju平台订单号
     * @return
     */
    PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam,String outTradeNo);
}
