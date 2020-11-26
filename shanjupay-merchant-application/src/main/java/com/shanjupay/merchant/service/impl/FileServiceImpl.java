package com.shanjupay.merchant.service.impl;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.BatchUpdateException;


@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${oss.qiniu.url}")
    private String qiniuUrl;

    @Value("${oss.qiniu.accessKey}")
    private String accessKey;

    @Value("${oss.qiniu.secretKey}")
    private String secretKey;

    @Value("${oss.qiniu.bucket}")
    private String bucket;

    @Value("${oss.qiniu.domainOfBucket}")
    private String domainOfBucket;

    @Override
    public String upload(byte[] bytes, String fileName) throws BatchUpdateException {
        try{
            QiniuUtils.upload2Qiniu(accessKey,secretKey,bucket,bytes,fileName);
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }

        // 返回文件名称
        return fileName+fileName;
    }

    @Override
    public String download(String fileName) throws BatchUpdateException {
        String downloadUrl=null;
        try{
            downloadUrl = QiniuUtils.download2Qiniu(accessKey, secretKey, fileName, domainOfBucket);
        }catch (Exception e){
            throw new BusinessException(CommonErrorCode.E_1001066);
        }
        return downloadUrl;
    }
}
