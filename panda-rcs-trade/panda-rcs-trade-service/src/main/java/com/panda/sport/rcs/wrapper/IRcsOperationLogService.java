package com.panda.sport.rcs.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.enums.OperationLogFlagEnum;
import com.panda.sport.rcs.pojo.RcsOperationLog;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.wrapper
 * @Description :  日志类
 * @Date: 2020-05-10 21:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface IRcsOperationLogService extends IService<RcsOperationLog> {
    /**
     * 查询数据
     * @param operationLogFlagEnum
     * @param tournamentId
     * @return
     */
    List<RcsOperationLog> query(OperationLogFlagEnum operationLogFlagEnum,Integer tournamentLevel,Long tournamentId);


    /**
     * 插入数据
     * @param operationLogFlagEnu
     * @param updatePreContent
     * @param updateContent
     * @param showContent
     */
    void insert(OperationLogFlagEnum operationLogFlagEnu,String suffixHandleCode,String hanlerId,String updatePreContent,String updateContent,String showContent);
}
