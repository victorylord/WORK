package com.panda.sport.rcs.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RiskMerchantManager;
import com.panda.sport.rcs.pojo.StandardSportType;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.riskmerchantmanager.UserChangeTagVo;
import com.panda.sports.api.vo.ShortSysUserVO;

import java.util.List;

/**
 * <p>
 * 商户管控记录表 服务类
 * </p>
 *
 * @author lithan
 * @since 2022-03-27
 */
public interface IRiskMerchantManagerService extends IService<RiskMerchantManager> {


    List<RiskMerchantManager> list(RiskMerchantManagerQueryVo param);

    IPage<RiskMerchantManagerVo> pageList(RiskMerchantManagerQueryVo param);

    int updateStatus(RiskMerchantManagerUpdateVo param);

    void changeUserTag(UserChangeTagVo vo);

    /**
     * 保存商户审核数据
     * @param userId
     * @param type     风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     * @param recommendValue  风控建议设置值
     * @param merchantShowValue 商户后台显示值(备用)
     * @param supplementExplain 风控补充说明（前端传过来的备注，可能为null）
     * @param requestData 代码需要处理的json值
     * @param status   状态:0待处理,1同意,2拒绝,3强制执行
     * @return
     * @return
     */
    Boolean initRiskMerchantManager(Long userId, Integer type, String recommendValue, String merchantShowValue,
                                           String supplementExplain, String requestData, Integer status);


    /**
     * excel批量导入更新接口
     * @param voList
     * @return
     */
    Boolean batchUpdateStatus(List<RiskMerchantImportUpdateVo> voList);

    /**
     * 根据sportType获取对应名称
     * @return
     */
    String getRecommendValue(Integer type, String content, List<StandardSportType> list);
}
