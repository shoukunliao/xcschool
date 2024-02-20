package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class TranscodeProcessor {

    @Autowired
    MediaProcessService mediaProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MinioClient minioClient;

    /**
     * videoprocess:
     * ffmpegpath: D:/soft/ffmpeg/ffmpeg.exe
     *
     * @throws Exception
     */
    @Value("videoprocess.ffmpegpath")
    private String ffmpegpath;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;


    @XxlJob("transcodeHandle")
    public void transcodeHandle() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //获取cpu核心数
        int count = Runtime.getRuntime().availableProcessors();
        //获取任务查询数据库
        List<MediaProcess> list = null;
        try {
            list = mediaProcessService.getAvailableProcesses(shardIndex, shardTotal, count);
            if (list == null || list.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("获取到{}个任务",list.size());
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(list.size());
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(list.size());
        for (MediaProcess process : list) {
            executorService.execute(() -> {
                try {
                    //获取fileid
                    Long id = process.getId();
                    //查询数据库
                    MediaFiles mediaFiles = mediaFileService.selectById(process.getFileId());
                    if (mediaFiles == null) {
                        log.debug("文件信息在db中不存在,fileMD5:{},bucket:{},path:{}", process.getFileId(), process.getBucket(), process.getFilePath());
                        return;
                    }
                    String filename = mediaFiles.getFilename();
                    String postfix = filename.substring(filename.lastIndexOf("."));
                    String mimeType = mediaFileService.getMimeType("postfix");
                    if (!"video/x-msvideo".equals(mimeType)) {
                        log.debug("不支持文件后缀:({}) 的视频转换", postfix);
                        return;
                    }
                    //开始任务
                    boolean flag = mediaProcessService.startTask(id);
                    if (!flag) {
                        log.debug("抢占任务失败,bucket:{},path:{}", process.getBucket(), process.getFilePath());
                        return;
                    }
                    //开始转换
                    //从minio中获取视频资源
                    File file = mediaFileService.downloadFileFromMinIO(process.getBucket(), process.getFilePath());
                    //视频转换
                    File tempFile = File.createTempFile(process.getBucket() + "/" + process.getFilePath(), ".temp");
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(tempFile));
                    File tempFile1 = File.createTempFile(process.getFilename(), ".mp4");
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegpath, tempFile.getAbsolutePath(), process.getFilename(), tempFile1.getAbsolutePath());
                    String s = mp4VideoUtil.generateMp4();

                    if (!"success".equals(s)) {
                        log.debug("文件转换失败!,bucket+path:{}", process.getBucket() + "/" + process.getFilePath());
                        //修改文件状态
                        //update media_process set status = '3' , fail_count = fail_count+1 ,errormsg = ? where id = ?
                        mediaProcessService.updateStatusOnFailure("文件转换失败", id);
                        return;
                    }
                    //上传到minio
                    //获取objectname
                    String objectname = getFilePathByMd5(process.getFileId(), ".mp4");

                    String mimeType1 = mediaFileService.getMimeType(".mp4");
                    boolean b = mediaFileService.addMediaFilesToMinio(tempFile1.getAbsolutePath(), objectname, bucket_videoFiles, mimeType1);
                    tempFile.delete();
                    tempFile1.delete();
                    if (!b) {
                        log.debug("上传文件失败!,bucket+path:{}", process.getBucket() + "/" + process.getFilePath());
                        mediaProcessService.updateStatusOnFailure("上传文件失败", id);
                        return;
                    }
                    //插入历史表
                    process.setId(null);
                    process.setStatus("2");
                    process.setFinishDate(LocalDateTime.now());
                    process.setFilePath(objectname);
                    process.setUrl(bucket_videoFiles + "/" + objectname);
                    int i = mediaProcessService.insert(process);
                    if (i <= 0) {
                        log.debug("插入历史表失败!,bucket+path:{}", process.getBucket() + "/" + process.getFilePath());
                        mediaProcessService.updateStatusOnFailure("插入历史表失败", id);
                        return;
                    }
                    //删除临时表
                    int i1 = mediaProcessService.deleteById(process.getId());
                    if (i1 <= 0) {
                        log.debug("删除临时表失败!,bucket+path:{}", process.getBucket() + "/" + process.getFilePath());
                        mediaProcessService.updateStatusOnFailure("删除临时表失败", id);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await(30, TimeUnit.MINUTES);
        }


    }

    private String getFilePathByMd5(String fileMd5, String extName) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extName;
    }

}
