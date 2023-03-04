package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsOperationLog;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import com.panda.sport.rcs.pojo.dto.UserHistoryReqVo;
import com.panda.sport.rcs.vo.RcsOperationLogHistory;
import com.panda.sport.rcs.vo.RcsUserException;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  日志
 * @Date: 2020-05-10 21:52
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsOperationLogMapper extends BaseMapper<RcsOperationLog> {
    int deleteByPrimaryKey(Integer id);


    int insertSelective(RcsOperationLog record);

    RcsOperationLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RcsOperationLog record);

    int updateByPrimaryKey(RcsOperationLog record);

    List<RcsOperationLog> queryByTournamentId(@Param("tournamentLevel") Integer tournamentLevel,@Param("tournamentId") Long tournamentId);

    IPage<RcsOperationLog> selectRcsOperationLog(IPage<RcsOperationLog> page,@Param("handlerId") String handlerId);

    IPage<RcsOperationLogHistory> selectRcsOperationLogByUser(IPage<RcsOperationLogHistory> page, @Param("user") String user, @Param("type")List<String> type, @Param("time")Long time,@Param("likeUser") String likeUser);

    List<RcsOperationLogHistory> selectRcsOperationLogByUserLimit(@Param("user") String user, @Param("type")List<String> type, @Param("startTime")Long startTime, @Param("endTime")Long endTime,@Param("likeUser") String likeUser, @Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize,@Param("merchantCodes") Set<String> merchantCodes);


    List<RcsOperationLogHistory> selectRcsOperationLogByUserLimit(@Param("req") UserHistoryReqVo user);

    Integer selectRcsOperationLogByUserLimitCount(@Param("req") UserHistoryReqVo user);

    List<RcsUserException> selectRcsOperationLogByOnLine(@Param("req") UserExceptionDTO req);

    Integer selectRcsOperationLogByGroupCount(@Param("req") UserExceptionDTO req);

    List<RcsUserException> selectRcsOperationLogByList(@Param("req") UserExceptionDTO req);
    List<RcsUserException> selectRcsOperationLogByGroup(@Param("req") UserExceptionDTO req);

    Integer selectRcsOperationLogByUserLimitCount(@Param("user") String user, @Param("type")List<String> type, @Param("time")Long time,@Param("likeUser") String likeUser);
    Integer selectRcsOperationLogByOnLineCount(@Param("req") UserExceptionDTO req);
    Integer getCurrentDayCount(@Param("user") String user, @Param("type")List<String> type, @Param("time")Long time,@Param("likeUser") String likeUser);

    List<RcsOperationLogHistory> selectRcsOperationLogToatlByUser( @Param("user") String user, @Param("type")List<String> type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("total")Integer total,@Param("likeUser") String likeUser);

    void saveBatchRcsOperationLog(@Param("rcsOperationLogList")List<RcsOperationLog> rcsOperationLogList);
}
