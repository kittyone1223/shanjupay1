package com.shanjupay.merchant.service;


import java.sql.BatchUpdateException;

/**
 * 文件上传
 */
public interface FileService {
    /**
     * 上传文件
     * @param bytes 文件字节
     * @param fileName 文件名称
     * @return  文件下载地址
     *
     */
    public String upload(byte[] bytes,String fileName) throws BatchUpdateException;

    public String download(String fileName) throws BatchUpdateException;
}
