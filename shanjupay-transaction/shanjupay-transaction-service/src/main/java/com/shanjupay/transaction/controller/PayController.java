package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.IPUtil;
import com.shanjupay.common.util.ParseURLPairUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-20 10:43
 **/

@Slf4j
@Controller
public class PayController {


    @Resource
    private TransactionService transactionService;

    @Reference
    private AppService appService;

    @RequestMapping(value = "/pay-entry/{ticket}")
    public String payEntry(@PathVariable("ticket") String ticket, HttpServletRequest request) throws Exception {


        // 工具类解析   （json对象）
        String ticketStr = EncryptUtil.decodeUTF8StringBase64(ticket);

        // 将ticket（json） 转成对象
        PayOrderDTO payOrderDTO = JSON.parseObject(ticketStr, PayOrderDTO.class);

        // 将对象转换为url格式
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);


        //解析客户端的类型（微信 支付宝）

        BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));

        switch (browserType) {
            case ALIPAY: // 直接跳转到收银台 pay.html
                return "forward:/pay-page?" + params;
            case WECHAT:
                return "forward:/pay-page?" + params;
            default:
        }

        //  客户端类型不支持  转换到错误页面
        return "forward:/pay-page-error";
    }

    /**
     * 支付宝的下单接口 前端订单确认页面  点击确认支付  请求进来
     *
     * @param orderConfirmVO 订单信息
     * @param response
     * @param request
     */
    // 支付宝下单接口
    @PostMapping("/createAliPayOrder")
    public void createAlipayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletResponse response, HttpServletRequest request) throws Exception {
        AppDTO app = appService.getAppById(orderConfirmVO.getAppId());
        Long merchantId = app.getMerchantId();
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        payOrderDTO.setMerchantId(merchantId);  // 商户id

        // 把前端输入的元转换为分
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount().toString())));

        // 客户端的id
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));

        PaymentResponseDTO<String> paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);

        // 支付宝下单接口的响应
        String content = paymentResponseDTO.getContent();


        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(content);
        response.getWriter().flush();
        response.getWriter().close();
    }
}
