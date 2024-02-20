package com.xuecheng;


import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BigFileTest {


    @Test
    public void testChunk() {
        //准备文件
        //"C:\Users\li'ao's'k\Desktop\test\31.第三十一节-Optional实战代码优化案例03.mp4"
        File file = new File("C:\\Users\\li'ao's'k\\Desktop\\test\\31.第三十一节-Optional实战代码优化案例03.mp4");
        //设置每块的最大容量 5MB
        int maxSize = 1024 * 1024 * 5;//单位为byte


        //输入流
        try {
            RandomAccessFile fileInputStream = new RandomAccessFile(file, "r");
            //获取文件MD5值
            String md5 = DigestUtils.md5Hex(new FileInputStream(file));
            //C:\Users\li'ao's'k\Desktop\test\chunk  分块地址

            //计算分块数目
            double count = Math.ceil(file.length() * 1.0 / maxSize);
            int i = 0;
            byte[] box = new byte[1024];//1mb
            //创建文件夹
            String filePath = "C:\\Users\\li'ao's'k\\Desktop\\test\\chunks\\" + md5 + "\\";
            File fileChunk = new File(filePath);
            if (!fileChunk.exists()) {
                boolean mkdirs = fileChunk.mkdirs();
                System.out.println(mkdirs ? "文件夹创建成功" : "文件夹创建失败");
            }
            while (++i <= count) {
                //创建分块文件

                fileChunk = new File(filePath + i);
                RandomAccessFile fileOutputStream = new RandomAccessFile(fileChunk, "rw");
                int len = 0;
                while ((len = fileInputStream.read(box)) != -1) {
                    fileOutputStream.write(box, 0, len);
                    if (fileChunk.length() >= maxSize) {
                        fileOutputStream.close();
                        break;
                    }
                }
                //
            }
            fileInputStream.close();
            System.out.println("文件分块成功");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void mergeChunk() {
        //合并文件path
        String md5 = "178522c64060dfd36d988f4ecd7c97cf";
        String mergePath = "C:\\Users\\li'ao's'k\\Desktop\\test\\merge\\";
        File mergeDir = new File(mergePath);
        if (mergeDir.exists()) {
            mergeDir.delete();
        }
        mergeDir.mkdirs();
        File outFile = new File(mergePath + "1.mp4");

        File file = new File("C:\\Users\\li'ao's'k\\Desktop\\test\\chunks\\" + md5 + "\\");
        //获取文件
        File[] files = file.listFiles();
        //转换为list
        List<File> fileList = Arrays.asList(files);
        //排序
        fileList.sort(Comparator.comparingInt(file2 -> Integer.parseInt(file2.getName())));
        //输入流
        byte[] b = new byte[1024];
        //输入流
        try (FileInputStream fileInputStream = new FileInputStream("C:\\Users\\li'ao's'k\\Desktop\\test\\31.第三十一节-Optional实战代码优化案例03.mp4");
        ) {
            RandomAccessFile targetFile = new RandomAccessFile(outFile, "rw");
            for (File file1 : fileList) {
                RandomAccessFile currentFile = new RandomAccessFile(file1, "r");
                int len = 0;
                while ((len = currentFile.read(b)) != -1) {
                    targetFile.write(b, 0, len);
                }
                currentFile.close();
            }
            targetFile.close();

            //取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            //取出合并文件的md5进行比较
            FileInputStream mergeFileStream = new FileInputStream(outFile);
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            mergeFileStream.close();
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //测试文件分块方法
    @Test
    public void testChunk2() throws IOException {
        File sourceFile = new File("C:\\Users\\li'ao's'k\\Desktop\\test\\31.第三十一节-Optional实战代码优化案例03.mp4");
        String chunkPath = "C:\\Users\\li'ao's'k\\Desktop\\test\\chunks";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);
        //缓冲区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分块文件中写数据
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分块" + i);
            }

        }
        raf_read.close();

    }

    final static MinioClient minioClient =  MinioClient.builder().endpoint("http://127.0.0.1:9000").credentials("minioadmin","minioadmin").build();

    //将文件上传到minio
    @Test
    public void uploadChunk() {
        String md5 = "178522c64060dfd36d988f4ecd7c97cf";
        String chunkFolderPath = "C:\\Users\\li'ao's'k\\Desktop\\test\\chunks\\"+md5;
        File chunkFolder = new File(chunkFolderPath);
        //分块文件
        File[] files = chunkFolder.listFiles();
        //将分块文件上传至minio
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket("testbucket").object("chunk/" + i).filename(files[i].getAbsolutePath()).build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    //minio合并分块

    @Test
    public void test_merge() throws Exception {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(3)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder().bucket("testbucket").object("merge01.mp4").sources(sources).build();
        minioClient.composeObject(composeObjectArgs);

    }
    //清除分块文件
    @Test
    public void test_removeObjects(){
        //合并分块完成将分块文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(3)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("testbucket").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}

