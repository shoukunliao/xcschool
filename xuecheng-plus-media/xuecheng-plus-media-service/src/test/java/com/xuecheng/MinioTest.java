package com.xuecheng;


import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;



public class MinioTest {
    static MinioClient minioClient = MinioClient.builder()
            .endpoint("http://127.0.0.1:9000")
            .credentials("minioadmin","minioadmin")
            .build();
    @Test
    public void upload(){
        //测试上传
        try {
            //准备参数
            UploadObjectArgs testbucket = UploadObjectArgs.builder().bucket("mediafiles")
                    .object("test01.png")
                    .filename("C:\\Users\\li'ao's'k\\Pictures\\63804147853254.png")
                    .contentType("video/mp4")
                    .build();
            minioClient.uploadObject(testbucket);
            //完整性校验
            FileInputStream originInputStream = new FileInputStream("C:\\Users\\li'ao's'k\\Pictures\\63804147853254.png");
            String origin_md5 = DigestUtils.md5Hex(originInputStream);
            FileInputStream input = new FileInputStream("C:\\Users\\li'ao's'k\\Pictures\\2.png");
            String md5 = DigestUtils.md5Hex(input);
            if (!StringUtils.equals(md5,origin_md5)){
                throw new RuntimeException("文件不一致");
            }

            System.out.println("上传成功");
        } catch (RuntimeException |IOException | ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }

    @Test
    public FilterInputStream download(){
        //测试下载
        FilterInputStream inputStream =null;
        try {
            //准备参数
            GetObjectArgs testbucket = GetObjectArgs.builder().bucket("mediafiles")
                    .object("test01.png")
                    .build();
             inputStream = minioClient.getObject(testbucket);

            FileOutputStream output = new FileOutputStream(new File("C:\\Users\\li'ao's'k\\Pictures\\2.png"));

            IOUtils.copy(inputStream,output);

            System.out.println("下载成功");
        } catch ( IOException | ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
            System.out.println("下载失败");
        }
        return inputStream;

    }

    @Test
    public void Delete(){
        //测试下载
        try {
            //准备参数
            RemoveObjectArgs testbucket = RemoveObjectArgs.builder().bucket("mediafiles")
                    .object("test01.png")
                    .build();
            minioClient.removeObject(testbucket);

            System.out.println("删除成功");
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }

    }

}