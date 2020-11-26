package com.shanjupay.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.paymentagent.common.constant.AliCodeConstants;
import com.shanjupay.paymentagent.message.PayProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.api.AlipayConstants.APP_ID;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-21 11:13
 **/
@Slf4j
@Service
public class PayChannelAgentServiceImpl implements PayChannelAgentService {

    @Autowired
    private PayProducer payProducer;

    @Override
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) {


        log.info("支付宝渠道参数", alipayBean.toString());

        // 支付宝渠道参数

        String gateway = aliConfigParam.getUrl(); // 支付宝下单接口地址
        String appId = aliConfigParam.getAppId();  // appId
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();  // 私钥
        String format = aliConfigParam.getFormat();  // 数据格式
        String charest = aliConfigParam.getCharest();  // 字符编码
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey();  // 支付宝公钥
        String signtype = aliConfigParam.getSigntype();  // 签名算法类型
        String notifyUrl = aliConfigParam.getNotifyUrl();  // 支付结果通知地址
        String returnUrl = aliConfigParam.getReturnUrl();   // 支付完成返回商户地址


        // 构造sdk的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(gateway, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype); //获得初始化的
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的 request
        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(alipayBean.getOutTradeNo());
        model.setTotalAmount(alipayBean.getTotalAmount());  // 订单金额（元）
        model.setSubject(alipayBean.getSubject());
        model.setProductCode("QUICK_WAP_PAY");
        model.setBody(alipayBean.getBody());
        model.setTimeoutExpress(alipayBean.getExpireTime()); //设置国企时间
        alipayRequest.setBizModel(model);


        // 设置异步通知地址
        alipayRequest.setNotifyUrl(notifyUrl);

        // 设置同步地址
        alipayRequest.setReturnUrl(returnUrl);


        //alipayRequest.setBizContent("{" + " \"out_trade_no\":\"20150320010101123\"," + " \"total_amount\":\"0.01\"," + " \"subject\":\"Iphone6 16G\"," + " \"product_code\":\"QUICK_WAP_PAY\"" + " }");//填充业务参数
//        String form = "";
//        try {
//            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//
//
//        httpResponse.setContentType("text/html;charset=" + CHARSET);
//        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();
        try {

            // 请求支付宝的下单接口 发起http请求
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
            PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
            paymentResponseDTO.setContent(response.getBody()); // 支付宝的响应结果

           // 向mq发送一条延迟消息
            PaymentResponseDTO<AliConfigParam> notice = new PaymentResponseDTO<>();
            notice.setOutTradeNo(alipayBean.getOutTradeNo());  // 闪聚平台的订单
            notice.setContent(aliConfigParam);
            notice.setMsg("ALIPAY_WAP");   // 表示查询支付宝的接口

            //发送消息
            payProducer.payOrderNotice(notice);

            return paymentResponseDTO;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400002);
        }
    }

    /**
     * 查询支付宝交易状态
     * @param aliConfigParam  支付宝渠道
     * @param outTradeNo  shanju平台订单号
     * @return
     */
    @Override
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) {


        String gateway = aliConfigParam.getUrl(); // 支付宝接口网关地址
        String appId = aliConfigParam.getAppId();  //appid
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey(); // 私钥
        String format = aliConfigParam.getFormat();  // json格式
        String charest = aliConfigParam.getCharest();//编码 utf‐8
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //公钥
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        log.info("C扫B请求支付宝查询订单，参数：{}", JSON.toJSONString(aliConfigParam));


        // 构建sdk客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gateway, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype);

        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();

        //闪聚平台订单号
        model.setOutTradeNo(outTradeNo);
        // 封装请求参数
        queryRequest.setBizModel(model);

        // 请求支付宝接口
        PaymentResponseDTO dto = null;
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);

            // 支付宝响应的code   10000表示成功
            String code = response.getCode();
            if (AliCodeConstants.SUCCESSCODE.equals(response.getCode())) {

                // 将支付宝响应的状态转化成闪聚平台的状态
                TradeStatus tradeStatus = covertAliTradeStatusToShanjuCode(response.getTradeStatus());
                dto = PaymentResponseDTO.success(response.getTradeNo(), response.getOutTradeNo(), tradeStatus, response.getMsg() + " " + response.getSubMsg());
                log.info("‐‐‐‐查询支付宝H5支付结果" + JSON.toJSONString(dto));
                return dto;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        dto = PaymentResponseDTO.fail("查询支付宝支付结果异常", outTradeNo, TradeStatus.UNKNOWN);
        return dto;
    }

    /**
     * 将支付宝查询时订单状态trade_status 转换为 闪聚订单状态
     *
     * @param aliTradeStatus 支付宝交易状态 *
     *                       WAIT_BUYER_PAY（交易创建，等待买家付款）
     *                       TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）
     *                       TRADE_SUCCESS（交易支付成功）
     *                       TRADE_FINISHED（交易结束，不可退款）
     * @return
     */
    private TradeStatus covertAliTradeStatusToShanjuCode(String aliTradeStatus) {
        switch (aliTradeStatus) {
            case AliCodeConstants.WAIT_BUYER_PAY:
                return TradeStatus.USERPAYING;
            case AliCodeConstants.TRADE_SUCCESS:
            case AliCodeConstants.TRADE_FINISHED:
                return TradeStatus.SUCCESS;
            default:
                return TradeStatus.FAILED;
        }
    }
}
