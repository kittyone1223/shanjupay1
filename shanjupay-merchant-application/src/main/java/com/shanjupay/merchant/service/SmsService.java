package com.shanjupay.merchant.service;

public interface SmsService {

    /**
     * 获取验证码
     * @param phone
     * @return
     */
    String sendSms(String phone);


    void checkVerifiyCode(String verifiyKey,String verifiyCode);

}
