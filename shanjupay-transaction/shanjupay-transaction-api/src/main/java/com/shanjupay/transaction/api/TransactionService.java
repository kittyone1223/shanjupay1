package com.shanjupay.transaction.api;


import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;

/**
 * 交易订单相关服务接口
 */
public interface TransactionService {

    /**
     * 生成门店二维码
     *
     * @param qrCodeDto 传入merchantId,appId、storeid、channel、subject、body
     * @return 支付入口url   将二维码的参数组成json并用base64编码
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException;


    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO);

    /**
     * 根据订单号查询订单
     * @param tradeNo
     * @return
     */
    public PayOrderDTO queryPayOrder(String tradeNo);

    /**
     * 更新订单支付状态
     * @param tradesNo     闪聚平台订单号
     * @param payChannnelTradeNo   支付宝或者微信的交易流水号
     * @param state   订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成 功,4‐关闭 5‐‐失败
     * @throws BusinessException
     */
    public void updateOrderTradeNoAndTradeState(String tradesNo,String payChannnelTradeNo,String state) throws BusinessException;
}
