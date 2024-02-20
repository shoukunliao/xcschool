package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    @Select("select * from media_process  where status in ('1','3') and fail_count <= 3 and id % #{shardTotal} = #{shardIndex} limit #{limit}")
    List<MediaProcess> getAvailableProcesses(@Param("shardIndex") Integer shardIndex, @Param("shardTotal") Integer shardTotal, @Param("limit") Integer limit);

    @Update("update media_process set status = '4' where status in ('1','3') and fail_count <= 3 and id = #{id}")
    int startTask(Long id);


    @Update("update media_process set status = #{status} , fail_count = fail_count + #{offset} ,errormsg = #{errorMsg} where id =  #{id} ")
    void updateStatusById(String status, String errorMsg, Long id, Integer offset);

    @Insert("insert into media_process_history(file_id, filename, bucket, status, create_date, finish_date, url, file_path, errormsg)" +
            "values(#{fileId},#{filename},#{bucket},#{status},#{createDate},#{finishDate},#{url},#{filePath},#{errormsg}) ")
    int insertHistory(MediaProcess process);
}
