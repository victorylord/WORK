package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 联赛模板margiain引用表
 * @Param
 * @Author toney
 * @Date 20:02 2020/5/10
 * @return
 **/
@Repository
public interface RcsTournamentTemplatePlayMargainRefMapper extends BaseMapper<RcsTournamentTemplatePlayMargainRef> {

    /**
     * 插入分时margin参数配置数据
     *
     * @param margainRefList
     * @return
     */
    int insertBatch(@Param("list") List<RcsTournamentTemplatePlayMargainRef> margainRefList);

    /**
     * 根据id修改分时margin参数数据
     *
     * @param param
     * @return
     */
    int updatePlayMargainRefById(RcsTournamentTemplatePlayMargainRef param);
    /**
     * 根据赛事id修改分时status
     *
     * @param matchIds
     * * @param status
     * @return
     */
    int updatePlayMargainRefStatusByMatchIds(@Param("matchIds") List<Long> matchIds,@Param("status") Integer status);
    /**
     * 获取早盘上一个节点的margin值
     * @param param
     * @return
     */
    RcsTournamentTemplatePlayMargainRef selectPreLastPlayMargainRef(RcsTournamentTemplatePlayMargainRef param);

    /**
     * 获取滚球上一个节点的margin值
     * @param param
     * @return
     */
    RcsTournamentTemplatePlayMargainRef selectLiveLastPlayMargainRef(RcsTournamentTemplatePlayMargainRef param);

    /**
     * @Description: 将此赛事有效分时margin的状态更新为3,定时任务同步刷新配置
     * @Author  carver
     * @Date  2020/10/29 17:23
     * @param id:
     * @return: java.util.List<com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef>
     **/
    List<RcsTournamentTemplatePlayMargainRef> selectMarginRefByTemplateId(Long id);
}