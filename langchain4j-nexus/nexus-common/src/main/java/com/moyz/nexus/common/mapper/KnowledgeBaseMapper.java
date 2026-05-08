package com.moyz.nexus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.nexus.common.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 搜索知识库（管理员）
     *
     * @param keyword 关键�?
     * @return
     */
    Page<KnowledgeBase> searchByAdmin(Page<KnowledgeBase> page, @Param("keyword") String keyword);

    /**
     * 搜索知识库（用户�?
     *
     * @param ownerId 用户id
     * @param keyword 关键�?
     * @return
     */
    Page<KnowledgeBase> searchByUser(Page<KnowledgeBase> page, @Param("ownerId") long ownerId, @Param("keyword") String keyword, @Param("includeOthersPublic") Boolean includeOthersPublic);

    /**
     * 根据知识点获取知识库信息
     *
     * @param itemUuid 知识点uuid
     * @return
     */
    KnowledgeBase getByItemUuid(@Param("itemUuid") String itemUuid);

    /**
     * 更新统计数据
     *
     * @param uuid
     */
    void updateStatByUuid(@Param("uuid") String uuid, @Param("embeddingCount") int embeddingCount);

    Integer countCreatedByTimePeriod(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    Integer countAllCreated();
}
