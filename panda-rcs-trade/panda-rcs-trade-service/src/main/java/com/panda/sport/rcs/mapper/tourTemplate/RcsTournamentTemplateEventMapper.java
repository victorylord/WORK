package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * @Description 联赛模板事件主表
 * @Param
 * @Author toney
 * @Date 20:01 2020/5/10
 * @return
 **/
@Repository
public interface RcsTournamentTemplateEventMapper extends BaseMapper<RcsTournamentTemplateEvent> {
    /**
     * 批量插入滚球结算审核事件
     * @param list
     * @return
     */
    int insertBatch(@Param("list") List<RcsTournamentTemplateEvent> list);

    /**
     * 获取滚球事件
     * @param templateId
     * @param sportId
     * @return
     */
    List<RcsTournamentTemplateEvent> getTournamentTemplateEventList(@Param("templateId") Long templateId, @Param("sportId") Integer sportId);

    /**
     * 根据id修改滚球结算审核事件数据
     * @param param
     * @return
     */
    int updateTemplateEventById(RcsTournamentTemplateEvent param);
}