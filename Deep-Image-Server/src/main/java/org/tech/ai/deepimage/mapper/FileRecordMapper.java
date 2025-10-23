package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tech.ai.deepimage.entity.FileRecord;
import org.tech.ai.deepimage.model.dto.response.TrashStatsResponse;

import java.util.List;

/**
 * 文件记录表 Mapper 接口
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {

    /**
     * 批量更新删除标记
     */
    int batchUpdateDeleteFlag(@Param("fileIds") List<Long> fileIds,
                               @Param("userId") Long userId,
                               @Param("deleteFlag") Integer deleteFlag);

    /**
     * 查询回收站文件（忽略 @TableLogic，支持分页）
     */
    Page<FileRecord> selectTrashFiles(
            Page<FileRecord> page,
            @Param("userId") Long userId
    );

    /**
     * 批量彻底删除文件
     */
    int permanentDeleteBatch(@Param("fileIds") List<Long> fileIds, @Param("userId") Long userId);

    /**
     * 查询回收站统计信息
     */
    TrashStatsResponse selectTrashStats(@Param("userId") Long userId);

    /**
     * 查询用户回收站所有文件ID
     */
    List<Long> selectTrashFileIds(@Param("userId") Long userId);

    /**
     * 查询回收站中的指定文件（绕过 @TableLogic）
     */
    List<FileRecord> selectTrashFilesByIds(@Param("fileIds") List<Long> fileIds, @Param("userId") Long userId);
}
