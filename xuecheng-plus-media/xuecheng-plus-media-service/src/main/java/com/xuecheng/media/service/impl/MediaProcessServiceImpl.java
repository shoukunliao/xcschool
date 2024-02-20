package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Override
    public List<MediaProcess> getAvailableProcesses(Integer shardIndex, Integer shardTotal,Integer limit) {
        List<MediaProcess> availableProcesses = mediaProcessMapper.getAvailableProcesses(shardIndex, shardTotal,limit);
        return availableProcesses;


    }

    @Override
    public boolean startTask(Long id) {
        int i = mediaProcessMapper.startTask(id);
        return i<=0?false:true;
    }

    @Override
    public void updateStatusOnFailure(String errorMsg, Long id) {
        mediaProcessMapper.updateStatusById("3",errorMsg,id,1);
    }

    @Override
    public void updateStatusOnSuccess(Long id) {
        mediaProcessMapper.updateStatusById("2",null,id,0);
    }

    @Override
    public int insert(MediaProcess process) {
        return mediaProcessMapper.insertHistory(process);
    }

    @Override
    public int deleteById(Long id) {
        return mediaProcessMapper.deleteById(id);
    }
}
