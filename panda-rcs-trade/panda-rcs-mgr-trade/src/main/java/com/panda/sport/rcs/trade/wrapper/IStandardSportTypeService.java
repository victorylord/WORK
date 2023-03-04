package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportType;

import java.util.List;

/**
 * <p>
 * 标准体育种类表.  服务类
 * </p>
 *
 * @author author
 * @since 2019-09-27
 */
public interface IStandardSportTypeService extends IService<StandardSportType> {

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportType>
     * @Description 查询所有数据
     * @Param []
     * @Author kimi
     * @Date 2019/10/5
     **/

    List<StandardSportType> getStandardSportTypeList();
}
