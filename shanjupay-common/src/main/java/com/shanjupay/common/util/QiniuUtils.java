package com.shanjupay.common.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class QiniuUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);



    // 工具方法 上传文件
    public static void test(){
        //构造一个带指定 Region 对象的配置类  指定存储区域   要和七牛云设置的区域保持一致
        Configuration cfg = new Configuration(Region.huanan());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "QskzIgBrEHi8eYerxvqc4Jyk__pYQDz71fQOaXdw";
        String secretKey = "xd2ASVgLlGGQVP7EBCS3xm48qIqYpkuJGKS6WnyI";
        String bucket = "shanjupay-wx";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = UUID.randomUUID()+ ".jpg";
        try {
            String filePath = "D:\\img\\timg.jpg";
            FileInputStream fis = new FileInputStream(new File(filePath));
            byte[] bytes = IOUtils.toByteArray(fis);


            //byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
            Auth auth = Auth.create(accessKey, secretKey);
            // 令牌
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            //ignore
        }
    }




    private static void download() {
        String fileName = "a25c893d-91e6-442a-8298-8a6ebd4c98aa.jpg";
        String domainOfBucket = "http://qjospmbfq.hn-bkt.clouddn.com";
        String encodedFileName = null;
        try {
            encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        String accessKey = "QskzIgBrEHi8eYerxvqc4Jyk__pYQDz71fQOaXdw";
        String secretKey = "xd2ASVgLlGGQVP7EBCS3xm48qIqYpkuJGKS6WnyI";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
    }



    // 工具方法  文件上传
    public static void upload2Qiniu(String accessKey,String secretKey,String bucket,byte[] bytes,String fileName){
        //构造一个带指定 Region 对象的配置类  指定存储区域   要和七牛云设置的区域保持一致
        Configuration cfg = new Configuration(Region.huanan());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);

        String key = fileName;
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(bytes, key, uploadToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException e) {
            Response r = e.response;
            LOGGER.error(r.toString());
            try {
                LOGGER.error(r.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException(r.toString());
        }

    }

    // 工具方法 下载
    public static String download2Qiniu(String accessKey,String secretKey,String fileName,String domainOfBucket){

        String encodedFileName = null;
        try {
            encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
        return finalUrl;

    }


    public static void main(String[] args) {
        //QiniuUtils.download();
        String orginUrl = "a25c893d-91e6-442a-8298-8a6ebd4c98aa.jpg";
        int i = orginUrl.lastIndexOf(".");
        System.out.println(i);
        String substring = orginUrl.substring(i);
        System.out.println(substring);
        System.out.println();

    }
}
