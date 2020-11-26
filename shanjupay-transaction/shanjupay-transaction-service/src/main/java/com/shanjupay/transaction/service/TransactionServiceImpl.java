package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @program: shanjupay
 * @author: Mr.Wang
 * @create: 2020-11-18 19:03
 **/

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {


    @Value("${shanjupay.payurl}")
    private String payurl;

    @Reference
    MerchantService merchantService;


    @Reference
    AppService appService;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Reference
    private PayChannelAgentService payChannelAgentService;

    @Reference
    PayChannelService payChannelService;

    /**
     * 生成门店二维码
     *
     * @param qrCodeDto 传入merchantId,appId、storeid、channel、subject、body
     * @return
     * @throws BusinessException
     */
    @Override
    public String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException {
        //校验应用和门店
        verifyAppAndStore(qrCodeDto.getMerchantId(), qrCodeDto.getAppId(), qrCodeDto.getStoreId());

        // 1.生成支付信息
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
        payOrderDTO.setAppId(qrCodeDto.getAppId());
        payOrderDTO.setStoreId(qrCodeDto.getStoreId());
        payOrderDTO.setSubject(qrCodeDto.getSubject().toString());//    显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());//订单内容
        String jsonString = JSON.toJSONString(payOrderDTO);
        log.info("transaction service createStoreQRCode,JsonString is {}", jsonString);


        //将支付信息保存到票据中
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);

        // 支付入口
        String payEntryUrl = payurl+ticket;
        log.info("transaction service createStoreQRCode,pay‐entry is {}",payEntryUrl);
        return payEntryUrl;
    }


    /**
     *  保存支付宝订单  1.保存订单到shanju平台 2.调用支付渠道代理服务调用支付宝的接口
     * @param payOrderDTO
     * @return
     */

    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) {
        // 保存订单到shanju支付平台数据库
        payOrderDTO.setPayChannel("ALIPAY_WAP");
        // 保存订单
        PayOrderDTO payOrderDTO1 = saveOrder(payOrderDTO);


        // 调用支付渠道代理服务支付宝下单接口
        PaymentResponseDTO paymentResponseDTO = aliPayH5(payOrderDTO1.getTradeNo());


        return paymentResponseDTO;
    }


    @Override
    public PayOrderDTO queryPayOrder(String tradeNo) {
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getTradeNo,tradeNo);
        PayOrder payOrder = payOrderMapper.selectOne(wrapper);
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }

    /**
     * 更新订单状态
     * @param tradesNo     闪聚平台订单号
     * @param payChannnelTradeNo   支付宝或者微信的交易流水号
     * @param state   订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成 功,4‐关闭 5‐‐失败
     * @throws BusinessException
     */
    @Override
    public void updateOrderTradeNoAndTradeState(String tradesNo, String payChannnelTradeNo, String state) throws BusinessException {
        LambdaUpdateWrapper<PayOrder> payOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        payOrderLambdaUpdateWrapper.eq(PayOrder::getTradeNo,tradesNo).set(PayOrder::getTradeState,state).set(PayOrder::getPayChannelTradeNo,payChannnelTradeNo);
        payOrderMapper.update(null,payOrderLambdaUpdateWrapper);
    }


    /**
     *  调用支付宝渠道代理
     * @param tradeNo
     * @return
     */
    private PaymentResponseDTO aliPayH5(String tradeNo){
        //构建支付实体
        AlipayBean alipayBean = new AlipayBean();

        //根据tradeNO来查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        alipayBean.setOutTradeNo(payOrderDTO.getTradeNo());
        alipayBean.setSubject(payOrderDTO.getSubject());

        //Integer totalAmount = payOrderDTO.getTotalAmount();
        String totalAmount=null;

        try {
            totalAmount = AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
        alipayBean.setTotalAmount(totalAmount);
        alipayBean.setBody(payOrderDTO.getBody());
        alipayBean.setStoreId(payOrderDTO.getStoreId());
        alipayBean.setExpireTime("30m");

        // 设置支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), payOrderDTO.getChannel(), "ALIPAY_WAP");

        if (payChannelParamDTO==null){
            throw new BusinessException(CommonErrorCode.E_300007);
        }

        // 支付宝渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), AliConfigParam.class);

        aliConfigParam.setCharest("utf-8");

        PaymentResponseDTO paymentResponse = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);
        return  paymentResponse;
    }




    /**
     * 保存订单到shanju平台
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    private PayOrderDTO saveOrder(PayOrderDTO payOrderDTO) throws BusinessException{

        PayOrder payOrder = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);
        //订单号
        payOrder.setTradeNo(PaymentUtil.genUniquePayOrderNo());//采用雪花片算法
        payOrder.setCreateTime(LocalDateTime.now());//创建时间
        payOrder.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));//过期时间是30分钟后
        payOrder.setCurrency("CNY");//人民币
        payOrder.setTradeState("0");//订单状态，0：订单生成
        payOrderMapper.insert(payOrder);//插入订单
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }



    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {
        // 判断应用是否属于当前商户
        Boolean aBoolean = appService.queryAppInMercahnt(appId, merchantId);
        if (!aBoolean) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }

        // 判断该门店是否属于某个商户
        Boolean aBoolean1 = merchantService.queryStoreInMercahnt(storeId, merchantId);
        if (!aBoolean1) {
            throw new BusinessException(CommonErrorCode.E_200006);
        }

    }
}
