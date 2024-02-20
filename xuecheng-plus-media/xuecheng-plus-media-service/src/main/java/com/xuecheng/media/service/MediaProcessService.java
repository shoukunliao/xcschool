package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaProcessService {


    List<MediaProcess> getAvailableProcesses(Integer shardIndex, Integer shardTotal,Integer limit);

    boolean startTask(Long id);

    void updateStatusOnFailure(String errorMsg, Long id);

    void updateStatusOnSuccess(Long id);

    int insert(MediaProcess process);

    int deleteById(Long id);
}
