package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tech.ai.deepimage.entity.FileRecord;
import org.tech.ai.deepimage.model.dto.response.TrashStatsResponse;

import java.util.List;

/**
 * File record table Mapper interface
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {

    /**
     * Batch update delete flag
     */
    int batchUpdateDeleteFlag(@Param("fileIds") List<Long> fileIds,
                               @Param("userId") Long userId,
                               @Param("deleteFlag") Integer deleteFlag);

    /**
     * Query recycle bin files (ignore @TableLogic, support pagination)
     */
    Page<FileRecord> selectTrashFiles(
            Page<FileRecord> page,
            @Param("userId") Long userId
    );

    /**
     * Batch permanently delete files
     */
    int permanentDeleteBatch(@Param("fileIds") List<Long> fileIds, @Param("userId") Long userId);

    /**
     * Query recycle bin statistics
     */
    TrashStatsResponse selectTrashStats(@Param("userId") Long userId);

    /**
     * Query all file IDs in user's recycle bin
     */
    List<Long> selectTrashFileIds(@Param("userId") Long userId);

    /**
     * Query specified files in recycle bin (bypass @TableLogic)
     */
    List<FileRecord> selectTrashFilesByIds(@Param("fileIds") List<Long> fileIds, @Param("userId") Long userId);
}
