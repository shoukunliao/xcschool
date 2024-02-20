package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    final
    MediaFilesMapper mediaFilesMapper;

    public MediaFileServiceImpl(MediaFilesMapper mediaFilesMapper, MinioClient minioClient) {
        this.mediaFilesMapper = mediaFilesMapper;
        this.minioClient = minioClient;
    }


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        //todo queryWrapper完善
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    private final MinioClient minioClient;

    @Autowired
    private MediaFileService currentProxy;


    /**
     * 上传文件
     *
     * @param companyId           机构id
     * @param uploadFileParamsDto 上传文件参数
     * @param localFilePath       本地文件路径
     * @return
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //获取零时文件路径localFilePath
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        //获取文件名称
        String filename = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //获取文件的mineType
        String mimeType = getMimeType(extension);
        //文件的MD5值
        String fileMd5 = getFileMd5(file);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 + extension;
        //将文件上传到minio
        boolean b = addMediaFilesToMinio(localFilePath, objectName, bucket_Files, mimeType);

        //文件大小
        uploadFileParamsDto.setFileSize(file.length());
        //将文件信息存储到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFileToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    @Override
    public MediaFiles addMediaFileToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket_files, String objectName) {
        //判断在数据库中是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            log.debug("该文件以及存在,bucket:{},ObjectName:{}", bucket_files, objectName);
            return mediaFiles;
        }
        //插入数据
        mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setUrl("/" + bucket_files + "/" + objectName);
        mediaFiles.setBucket(bucket_files);
        mediaFiles.setFilePath(objectName);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus("002003");
        mediaFiles.setStatus("1");
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            log.error("保存文件到数据库失败，{}", mediaFiles.toString());
            XueChengPlusException.cast("保存文件到数据库失败");
        }
        log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
        return mediaFiles;
    }

    @Override
    public boolean addMediaFilesToMinio(String localFilePath, String objectName, String bucket_files, String mimeType) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket_files)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .object(objectName).build());
            log.debug("上传文件成功,bucket:{},objectName:{}", bucket_files, objectName);
            return true;
        } catch (ErrorResponseException | InsufficientDataException | InvalidKeyException | InternalException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
            log.error("上传文件失败,bucket:{},objectName:{},错误原因:{}", bucket_files, objectName, e.getMessage());
            XueChengPlusException.cast("上传文件到文件系统失败");
            return false;
        }

    }

    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return simpleDateFormat.format(new Date()).replace("-", "/") + "/";

    }


    private String getFileMd5(File file) {
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        try {
            return DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String minType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            minType = extensionMatch.getMimeType();
        }
        return minType;
    }

    /**
     * 文件上传前检查
     *
     * @param fileMd5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //存储目录
            String filePath = mediaFiles.getFilePath();
            //文件流
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .build())) {
                ;

                if (stream != null) {
                    //文件已存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                XueChengPlusException.cast("检查文件异常");

            }
            //文件不存在
            return RestResponse.success(false);
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块前检查
     *
     * @param fileMd5
     * @param chunkIndex
     * @return
     */

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        //文件流
        try (InputStream fileInputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFilePath)
                        .build())) {


            if (fileInputStream == null) {
                //分块已存在
                return RestResponse.success(false);
            }
            return RestResponse.success(true);
        } catch (Exception e) {
//分块未存在
            return RestResponse.success(false);

        }
    }


    private String getChunkFileFolderPath(String fileMd5) {
        if (StringUtils.isEmpty(fileMd5)) {
            return "";
        }

        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/"+ fileMd5 +"chunk/";
    }

    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param bytes   文件字节
     * @return com.xuecheng.base.model.RestResponse
     * @description 上传分块
     * @author Mr.M
     * @date 2022/9/13 15:50
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {


        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        try {
            //将文件存储至minIO
            addMediaFilesToMinIO(bytes, bucket_videoFiles, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.debug("上传分块文件:{},失败:{}", chunkFilePath, ex.getMessage());
        }
        return RestResponse.validfail(false, "上传分块失败");
    }


    private void addMediaFilesToMinIO(byte[] bytes, String bucket_videoFiles, String chunkFilePath) {

        //分块文件
        //将分块文件上传至minio
        try {
            File tempFile = File.createTempFile("tempFile", ".temp");
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            boolean b = writeBuffer(fileOutputStream, bytes);
            if (!b) {
                XueChengPlusException.cast("临时文件写入失败");
            }
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket(bucket_videoFiles).object(chunkFilePath).filename(tempFile.getAbsolutePath()).build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块成功，chunkFilePath：{" + chunkFilePath + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //buffer【1024byte】写入操作
    private boolean writeBuffer(OutputStream fileOutputStream, byte[] bytes) {
        if (fileOutputStream == null) {
            XueChengPlusException.cast("临时文件不存在");
        }
        try {
            fileOutputStream.write(bytes);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    @Transactional
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //=====获取分块文件路径=====
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //组成将分块文件路径组成 List<ComposeSource>
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //=====合并=====
        //文件名称
        String fileName = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        try {
            //合并文件
            ObjectWriteResponse response = minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_videoFiles)
                            .object(mergeFilePath)
                            .sources(sourceObjectList)
                            .build());
            log.debug("合并文件成功:{}", mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "合并文件失败。");
        }

        // ====验证md5====
        File minioFile = downloadFileFromMinIO(bucket_videoFiles, mergeFilePath);
        if (minioFile == null) {
            log.debug("下载合并后文件失败,mergeFilePath:{}", mergeFilePath);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }

        try (InputStream newFileInputStream = new FileInputStream(minioFile)) {
            //minio上文件的md5值
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if (!fileMd5.equals(md5Hex)) {
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        } finally {
            if (minioFile != null) {
                minioFile.delete();
            }
        }

        //文件入库
        currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, mergeFilePath);
        //添加视频处理信息
        currentProxy.addMediaFilesProcessToDb(fileMd5,uploadFileParamsDto,bucket_videoFiles,mergeFilePath);
        //=====清除分块文件=====
        clearChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }


    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Transactional
    @Override
    public void addMediaFilesProcessToDb(String md5,UploadFileParamsDto uploadFileParamsDto, String bucket, String path){
        MediaProcess mediaProcess = new MediaProcess();
        BeanUtils.copyProperties(uploadFileParamsDto,mediaProcess);
        mediaProcess.setBucket(bucket);
        mediaProcess.setFileId(md5);
        mediaProcess.setFilePath(path);
        mediaProcess.setStatus("1");
        mediaProcess.setCreateDate(LocalDateTime.now());
        //入库
        int insert = mediaProcessMapper.insert(mediaProcess);
        if (insert<=0){
            log.debug("添加FileProcess失败,bucket:{},path:{}",bucket,path);
            XueChengPlusException.cast("添加FileProcess失败");
        }
        log.debug("添加FileProcess成功,bucket:{},path:{}",bucket,path);



    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        List<DeleteObject> objects = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i ->
                        new DeleteObject(chunkFileFolderPath + i)
                ).collect(Collectors.toList());

        RemoveObjectsArgs build = RemoveObjectsArgs.builder().bucket(bucket_videoFiles).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(build);
        results.forEach(deleteErrorResult -> {
            try {
                DeleteError deleteError = deleteErrorResult.get();
                XueChengPlusException.cast("bunck文件" + deleteError.objectName() + "\\" + deleteError.objectName() + "删除失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 添加media信息到Db
     *
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param bucket_videoFiles
     * @param mergeFilePath
     */
    @Override
    @Transactional
    public void addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket_videoFiles, String mergeFilePath) {
        MediaFiles mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setBucket(bucket_videoFiles);
        mediaFiles.setFilePath(mergeFilePath);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setUrl(bucket_videoFiles + "/" + mergeFilePath);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus("002002");
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            XueChengPlusException.cast("大文件信息插入Db失败");
        }
        log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
    }

    @Override
    public File downloadFileFromMinIO(String bucket_videoFiles, String mergeFilePath) {
        FileOutputStream output = null;
        InputStream response = null;
        try {
            response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_videoFiles)
                    .object(mergeFilePath)
                    .build());
            //创建临时文件
            File minioFile = File.createTempFile("minioFile", ".temp");
            output = new FileOutputStream(minioFile);
            IOUtils.copy(response, output);

            return minioFile;

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public MediaFiles selectById(String fileId) {
        return mediaFilesMapper.selectById(fileId);
    }


    private String getFilePathByMd5(String fileMd5, String extName) {


        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extName;
    }


}