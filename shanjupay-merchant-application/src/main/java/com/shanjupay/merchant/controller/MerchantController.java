package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;

import com.shanjupay.common.util.QRCodeUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;

import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.UUID;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@Slf4j
@Api(value = "商户平台应用接口", tags = "商户平台应用接口", description = "商户平台应用接口")
public class MerchantController {

    @org.apache.dubbo.config.annotation.Reference
    MerchantService merchantService;

    @ApiOperation(value = "根据id查询商户信息")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {

        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }


    @ApiOperation("获取登录用户的商户信息")
    @GetMapping(value = "/my/merchants")
    public MerchantDTO getMyMerchantInfo() {
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }


    @ApiOperation("测试")
    @GetMapping(path = "/hello")
    public String hello() {
        return "hello";
    }

    @ApiOperation("测试")
    @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string")
    @PostMapping(value = "/hi")
    public String hi(String name) {
        return "hi," + name;
    }


    @Autowired
    private SmsService smsService;


    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String", paramType = "query")
    @GetMapping("/sms")
    public String getSmsCode(@RequestParam String phone) {
        log.info("发送验证码手机号{}", phone);
        return smsService.sendSms(phone);
    }


    @ApiOperation("注册服务")
    @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO) {
        // 1. 校验
        if (merchantRegisterVO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }

        if (StringUtils.isEmpty(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        /**
         * 校验手机号格式
         */
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        // 联系人不能为空
        if (StringUtils.isEmpty(merchantRegisterVO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }

        // 用户密码非空验证
        if (StringUtils.isEmpty(merchantRegisterVO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }

        // 验证码不能为空
        if (StringUtils.isEmpty(merchantRegisterVO.getVerifiyCode()) || StringUtils.isEmpty(merchantRegisterVO.getVerifiykey())) {
            throw new BusinessException(CommonErrorCode.E_100103);
        }


        // 校验验证码
        smsService.checkVerifiyCode(merchantRegisterVO.getVerifiykey(), merchantRegisterVO.getVerifiyCode());
        // 注册商户
//        MerchantDTO merchantDTO = new MerchantDTO();
//        merchantDTO.setUsername(merchantRegisterVO.getUsername());
//        merchantDTO.setMobile(merchantRegisterVO.getMobile());
//        merchantDTO.setPassword(merchantRegisterVO.getPassword());
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.vo2dto(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }


    @Autowired
    private FileService fileService;

    @ApiOperation("证件上传")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file) {

        // 原始文件名称
        String originalFilename = file.getOriginalFilename();
        // 文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") - 1);
        // 文件名称
        String fileName = UUID.randomUUID().toString() + suffix;
        // 上传文件 返回下载url

        String fileUrl = null;

        try {
            fileUrl = fileService.upload(file.getBytes(), fileName);
        } catch (BatchUpdateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    @GetMapping("/download")
    @ApiOperation(value = "文件下载")
    public String download(String fileName) {
        String downloadUrl = null;
        try {
            downloadUrl = fileService.download(fileName);
        } catch (BatchUpdateException e) {
            e.printStackTrace();
        }
        return downloadUrl;
    }


    // @ApiOperation("商户资质申请")
    //@ApiImplicitParams({@ApiImplicitParam(name = "merchantDetailInfo", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")})
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantDetailInfo) {

        //Bearer eyJtZXJjaGFudElkIjoxMzI2NTUzODM5MzQzNTE3Njk4fQ==
        // 解析token得到商户id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantDetailInfo);


        // 资质申请
        merchantService.applyMerchant(merchantId, merchantDTO);

    }



    @Reference
    private TransactionService transactionService;

    @Value("${shanjupay.c2b.body}")
    private String body;

    @Value("${shanjupay.c2b.subject}")
    private String subject;


    @GetMapping(value = "/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable String appId,@PathVariable Long storeId){
        // 商户id
        Long merchantId = SecurityUtil.getMerchantId();
        //生成二维码链接
        QRCodeDto qrCodeDto = new QRCodeDto();
        qrCodeDto.setMerchantId(merchantId);
        qrCodeDto.setAppId(appId);
        qrCodeDto.setStoreId(storeId);

        // 标题
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        // 商户名称
        String merchantName = merchantDTO.getMerchantName();

        // " %s商品"
        qrCodeDto.setSubject(String.format(subject,merchantName));

        // 内容 格式  “向%s付款”
        qrCodeDto.setBody(String.format(body,merchantDTO.getMerchantName()));

        String storeQRCodeUrl = transactionService.createStoreQRCode(qrCodeDto);
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        try {
            String qrCode = qrCodeUtil.createQRCode(storeQRCodeUrl, 200, 200);
            return qrCode;
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.E_200007);
//            e.printStackTrace();
        }

//        return null;
    }


}
