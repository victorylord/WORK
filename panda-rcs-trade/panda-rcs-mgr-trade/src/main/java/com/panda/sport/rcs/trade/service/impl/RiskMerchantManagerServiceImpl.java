package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RiskMerchantManagerMapper;
import com.panda.sport.rcs.mapper.TUserMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RiskMerchantManager;
import com.panda.sport.rcs.pojo.StandardSportType;
import com.panda.sport.rcs.pojo.TUser;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigsVo;
import com.panda.sport.rcs.trade.vo.userprofile.AddUserTagVo;
import com.panda.sport.rcs.trade.vo.userprofile.UserBetTagChangeReqVo;
import com.panda.sport.rcs.trade.wrapper.IStandardSportTypeService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.trade.wrapper.RcsUserConfigService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.RiskMerchantImportUpdateVo;
import com.panda.sport.rcs.vo.RiskMerchantManagerQueryVo;
import com.panda.sport.rcs.vo.RiskMerchantManagerUpdateVo;
import com.panda.sport.rcs.vo.RiskMerchantManagerVo;
import com.panda.sport.rcs.vo.riskmerchantmanager.UserChangeTagVo;
import com.panda.sport.rcs.vo.riskmerchantmanager.UserProfileUserTagChangeRecordVo;
import com.panda.sports.api.vo.ShortSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

/**
 * <p>
 * 商户管控记录表 服务实现类
 * </p>
 *
 * @author lithan
 * @since 2022-03-27
 */
@Service
@Slf4j
public class RiskMerchantManagerServiceImpl extends ServiceImpl<RiskMerchantManagerMapper, RiskMerchantManager> implements IRiskMerchantManagerService {

    @Autowired
    RiskMerchantManagerMapper riskMerchantManagerMapper;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    TUserMapper userMapper;

    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;

    @Autowired
    private RcsUserConfigService rcsUserConfigService;

    @Autowired
    private IStandardSportTypeService sportTypeService;

    @Override
    public List<RiskMerchantManager> list(RiskMerchantManagerQueryVo param) {
        QueryWrapper<RiskMerchantManager> queryWrapper = getQueryWrapper(param);
        return baseMapper.selectList(queryWrapper);
    }

    private QueryWrapper<RiskMerchantManager> getQueryWrapper(RiskMerchantManagerQueryVo param) {
        QueryWrapper<RiskMerchantManager> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isBlank(param.getUserName())) {
            queryWrapper.lambda().and(wrapper -> wrapper.eq(RiskMerchantManager::getUserName, param.getUserName())
                    .or().eq(RiskMerchantManager::getUserId, param.getUserName()));
        }
        if (!StringUtils.isBlank(param.getMerchantCode())) {
            queryWrapper.lambda().eq(RiskMerchantManager::getMerchantCode, param.getMerchantCode());
        }
        if (Objects.nonNull(param.getType())) {
            if (param.getType().equals(1)) {
                queryWrapper.lambda().in(RiskMerchantManager::getType, Arrays.asList(1, 6, 7));
            } else {
                queryWrapper.lambda().eq(RiskMerchantManager::getType, param.getType());
            }
        }
        queryWrapper.lambda().eq(Objects.nonNull(param.getStatus()), RiskMerchantManager::getStatus, param.getStatus())
                .eq(Objects.nonNull(param.getMerchantOperator()), RiskMerchantManager::getMerchantOperator, param.getMerchantOperator())
                .eq(Objects.nonNull(param.getRiskOperator()), RiskMerchantManager::getRiskOperator, param.getRiskOperator())
                .ge(Objects.nonNull(param.getRecommendStartTime()), RiskMerchantManager::getRecommendTime, param.getRecommendStartTime())
                .le(Objects.nonNull(param.getRecommendEndTime()), RiskMerchantManager::getRecommendTime, param.getRecommendEndTime())
                .ge(Objects.nonNull(param.getProcessStartTime()), RiskMerchantManager::getProcessTime, param.getProcessStartTime())
                .le(Objects.nonNull(param.getProcessEndTime()), RiskMerchantManager::getProcessTime, param.getProcessEndTime())
                //排序 1-平台建议时间 2-处理时间
                .orderBy(Objects.equals(1, param.getOrderKey()), Sort.Direction.ASC.name().equalsIgnoreCase(param.getOrderType()), RiskMerchantManager::getRecommendTime)
                .orderBy(Objects.equals(2, param.getOrderKey()), Sort.Direction.ASC.name().equalsIgnoreCase(param.getOrderType()), RiskMerchantManager::getProcessTime)
                //默认按建议时间从近到远排序
                .orderBy(Objects.isNull(param.getOrderKey()), Sort.Direction.ASC.name().equalsIgnoreCase(param.getOrderType()), RiskMerchantManager::getRecommendTime);
        return queryWrapper;
    }

    @Override
    public IPage<RiskMerchantManagerVo> pageList(RiskMerchantManagerQueryVo param) {
        if (Objects.isNull(param.getCurrentPage()) || param.getCurrentPage().equals(0)) {
            param.setCurrentPage(1);
            param.setPageSize(10);
        }
        IPage<RiskMerchantManager> iPage = new Page<>(param.getCurrentPage(), param.getPageSize());
        QueryWrapper<RiskMerchantManager> queryWrapper = getQueryWrapper(param);
        IPage<RiskMerchantManager> merchantManagerIPage = baseMapper.selectPage(iPage, queryWrapper);
        IPage<RiskMerchantManagerVo> resultPage = new Page<>(merchantManagerIPage.getCurrent(), merchantManagerIPage.getSize(), merchantManagerIPage.getTotal());
        List<RiskMerchantManagerVo> list = new ArrayList<>();
        List<RiskMerchantManager> records = merchantManagerIPage.getRecords();
        if (!CollectionUtils.isEmpty(merchantManagerIPage.getRecords())) {
            List<StandardSportType> sportTypeList = sportTypeService.list();
            records.forEach(obj -> {
                RiskMerchantManagerVo vo = new RiskMerchantManagerVo();
                BeanUtils.copyProperties(obj, vo);
                vo.setUserId(String.valueOf(obj.getUserId()));
                vo.setRecommendValue(getRecommendValue(vo.getType(), vo.getRecommendValue(), sportTypeList));
                list.add(vo);
            });
        }
        resultPage.setRecords(list);
        return resultPage;
    }

    @Override
    public int updateStatus(RiskMerchantManagerUpdateVo param) {
        log.info("::{}::updateStatus==请求参数:{}", CommonUtil.getRequestId(), JSON.toJSON(param));
        RiskMerchantManager merchantManager = this.baseMapper.selectById(param.getId());
        Assert.notNull(merchantManager, "数据不存在！");
        merchantManager.setStatus(param.getStatus());
        merchantManager.setMerchantOperator(param.getMerchantOperator());
        merchantManager.setMerchantRemark(param.getMerchantRemark());
        merchantManager.setProcessTime(System.currentTimeMillis());
        /**不同类型的审核 做不同类型的数据修改处理*/
        updateBusinessData(merchantManager);

        return baseMapper.updateById(merchantManager);
    }


    //更新对应业务数据
    private void updateBusinessData(RiskMerchantManager merchantManager) {
        if (merchantManager.getStatus().equals(RiskMerchantManagerStatusEnum.Type_2.getCode())) {
            log.info("::{}::商户拒绝:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(merchantManager));
            return;
        }
        //1.投注特征标签 向业务发送变更日志
        if (merchantManager.getType().equals(RiskMerchantManagerTypeEnum.Type_1.getCode())) {
            Map<String, Object> map = JSONObject.parseObject(merchantManager.getRequestData(), Map.class);
            sendUserLog(merchantManager.getUserId(), Long.valueOf(map.get("tagId").toString()), merchantManager.getRiskOperator(), map.get("remark").toString());
            producerSendMessageUtils.sendMessage("USER_TAG_CHANGE_TOPIC", "USER_TAG_CHANGE_TAG", map.get("userId").toString(), map);
            Map<String, Object> changeMap = new HashMap<>();
            changeMap.put("userId", merchantManager.getUserId());
            changeMap.put("tagId", map.get("tagId").toString());
            producerSendMessageUtils.sendMessage("RCS_LIMIT_USER_TAG_CHANGE","",merchantManager.getUserId().toString(), changeMap);
            log.info("::{}::商户统一,更新用户标签完成:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        } else if (merchantManager.getType().equals(RiskMerchantManagerTypeEnum.Type_6.getCode())) {
            UserBetTagChangeReqVo userBetTagChangeReqVo = JSONObject.parseObject(merchantManager.getRequestData(), UserBetTagChangeReqVo.class);
            userBetTagChangeReqVo.setSubmitType(3);
            producerSendMessageUtils.sendMessage("rcs_risk_merchant_manager_audit_tag", "", userBetTagChangeReqVo.getUserId(), userBetTagChangeReqVo);
            log.info("::{}::商户同意预警,发送mq完成:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(userBetTagChangeReqVo));
        } else if (merchantManager.getType().equals(RiskMerchantManagerTypeEnum.Type_7.getCode())) {
            AddUserTagVo addUserTagVo = JSONObject.parseObject(merchantManager.getRequestData(), AddUserTagVo.class);
            addUserTagVo.getTag().setRiskStatus(0);
            producerSendMessageUtils.sendMessage("rcs_risk_merchant_manager_task_tag_audit", "", addUserTagVo.getUserId().toString(), addUserTagVo);
            log.info("::{}::商户同意任务标签,发送mq完成:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(addUserTagVo));
        } else {
            //2.type=2,3,4,5为特殊限额和特殊管控的内容
            try {
                RcsUserSpecialBetLimitConfigsVo vo = JSONObject.parseObject(merchantManager.getRequestData(), RcsUserSpecialBetLimitConfigsVo.class);
                vo.setSubmitType(3);
                //jordan 新加字段 isTrade默认值ture
                rcsUserConfigService.updateRcsUserSpecialBetLimitConfigsVo(vo, vo.getTraderId(),true);
            } catch (Exception e) {
                log.error("::{}::{}",CommonUtil.getRequestId(),e.getMessage(),e);
            }
        }
    }
    /**
     * 修改用户标签
     *
     * @param vo
     */
    @Override
    public void changeUserTag(UserChangeTagVo vo) {
        Map<String, Object> map = new HashMap<>();
        //向业务发送变更日志
        map.put("type", 1);
        map.put("remark", vo.getRemark());
        map.put("userId", vo.getUserId());
        map.put("tagType", 2);
        map.put("tagId", vo.getTagId());
        //处理状态
        Integer status = 0;
        //强制执行
        if (vo.getSubmitType().equals(2) || vo.getSubmitType().equals(3)) {
            status = RiskMerchantManagerStatusEnum.Type_3.getCode();
            sendUserLog(vo.getUserId(), vo.getTagId().longValue(), vo.getRiskOperator(), vo.getRemark());
            producerSendMessageUtils.sendMessage("USER_TAG_CHANGE_TOPIC", "USER_TAG_CHANGE_TAG", vo.getUserId().toString(), map);
            Map<String, Object> changeMap = new HashMap<>();
            changeMap.put("userId", vo.getUserId());
            changeMap.put("tagId", map.get("tagId").toString());
            producerSendMessageUtils.sendMessage("RCS_LIMIT_USER_TAG_CHANGE","",vo.getUserId().toString(), changeMap);
            log.info("::{}::更新用户标签完成,强制执行:{}:{}",CommonUtil.getRequestId(), vo.getUserId(), vo.getTagId());
        } else if (vo.getSubmitType().equals(1)) {
            status = RiskMerchantManagerStatusEnum.Type_0.getCode();
        }
        if(vo.getSubmitType().equals(3) ){
            return;
        }
        initRiskMerchantManager(vo.getUserId(), vo.getType(), vo.getTagName(), vo.getTagName(), vo.getSupplementExplain(), JSONObject.toJSONString(map), status);
    }

    void sendUserLog(Long userId, Long tagId,String changeManner,String remark) {
        try {
            LambdaQueryWrapper<TUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(TUser::getUid, userId);
            TUser user = userMapper.selectOne(userLambdaQueryWrapper);
            //记录变更日志
            UserProfileUserTagChangeRecordVo userProfileUserTagChangeRecord = new UserProfileUserTagChangeRecordVo();
            userProfileUserTagChangeRecord.setUserId(userId);
            //userProfileUserTagChangeRecord.setChangeBefore(userLevel);
            userProfileUserTagChangeRecord.setChangeAfter(tagId);
            userProfileUserTagChangeRecord.setChangeTag(tagId);
            //userProfileUserTagChangeRecord.setChangeDetail(vo.getUserId() + "用户新增了标签:" + tag.getTagName());
            userProfileUserTagChangeRecord.setChangeTime(System.currentTimeMillis());

            userProfileUserTagChangeRecord.setRealityValue("[{\"result\":\"" + remark + "@;@\",\"rule\":{ }}]");
            userProfileUserTagChangeRecord.setRemark(remark);
            userProfileUserTagChangeRecord.setStatus(1);
            userProfileUserTagChangeRecord.setChangeManner(changeManner);
            userProfileUserTagChangeRecord.setChangeType(2);
            userProfileUserTagChangeRecord.setTagType(2);
//            userProfileUserTagChangeRecord.setChangeValue("{}");
            userProfileUserTagChangeRecord.setUserName(user.getUsername());
            userProfileUserTagChangeRecord.setMerchantCode(user.getMerchantCode());
            userProfileUserTagChangeRecord.setOperateTime(System.currentTimeMillis());
            producerSendMessageUtils.sendMessage("rcs_risk_manual_tag_log", "rcs_risk_manual_tag_log_tag", userId.toString(), userProfileUserTagChangeRecord);
            log.info("::{}::手工修改标签日志通知:{}:{}",CommonUtil.getRequestId(), userId, JSONObject.toJSONString(userProfileUserTagChangeRecord));
            Thread.sleep(1000L);
        } catch (Exception e) {
            log.error("::{}::记录日志异常:{}:{}",CommonUtil.getRequestId(), userId, e.getMessage(),e);
        }

    }

    /**
     * 保存商户审核数据
     *
     * @param userId
     * @param type              风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     * @param recommendValue    风控建议设置值
     * @param merchantShowValue 商户后台显示值(备用)
     * @param supplementExplain 风控补充说明（前端传过来的备注，可能为null）
     * @param requestData       代码需要处理的json值
     * @param status            状态:0待处理,1同意,2拒绝,3强制执行
     * @return
     */
    @Override
    public Boolean initRiskMerchantManager(Long userId, Integer type, String recommendValue, String merchantShowValue,
                                           String supplementExplain, String requestData, Integer status) {
        //用户是否已有相同风控类型待处理，若是，则本次提交操作无效，不产生记录。系统提示“不可重复提交。当前用户已有相同风控类型操作待处理。”若否，则提交成功。
        if (status.equals(RiskMerchantManagerStatusEnum.Type_0.getCode())) {
            LambdaQueryWrapper<RiskMerchantManager> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RiskMerchantManager::getUserId, userId);
            queryWrapper.eq(RiskMerchantManager::getStatus, status);
            queryWrapper.eq(RiskMerchantManager::getType, type);
            List<RiskMerchantManager> queryList = riskMerchantManagerMapper.selectList(queryWrapper);
            if (ObjectUtils.isNotEmpty(queryList)) {
                throw new RcsServiceException("不可重复提交,用户已有相同风控类型操作待处理");
            }
        }

        ShortSysUserVO traderData = null;
        try {
            Integer sysUserId = TradeUserUtils.getUserId();
            traderData = rcsTradingAssignmentService.getShortSysUserById(sysUserId);
            if (traderData == null) {
                traderData = new ShortSysUserVO();
            }
        } catch (Exception e) {
            log.info("::{}::未获取到ShortSysUserVO",CommonUtil.getRequestId());
            traderData = new ShortSysUserVO();
            traderData.setUserCode("System");
        }

        try {
            RiskMerchantManager riskMerchantManager = new RiskMerchantManager();

            LambdaQueryWrapper<TUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(TUser::getUid, userId);
            TUser user = userMapper.selectOne(userLambdaQueryWrapper);
            riskMerchantManager.setUserId(userId);
            riskMerchantManager.setUserName(user.getUsername());
            riskMerchantManager.setMerchantCode(user.getMerchantCode());
            riskMerchantManager.setType(type);
            riskMerchantManager.setRiskOperator(traderData.getUserCode());
            riskMerchantManager.setRecommendTime(System.currentTimeMillis());
            riskMerchantManager.setRecommendValue(filterChar(recommendValue));
            riskMerchantManager.setMerchantShowValue(filterChar(merchantShowValue));
            riskMerchantManager.setSupplementExplain(supplementExplain);
            riskMerchantManager.setStatus(status);
            riskMerchantManager.setRequestData(requestData);
            riskMerchantManagerMapper.insert(riskMerchantManager);
            return true;
        } catch (Exception e) {
            log.error("::{}::RiskMerchantManager入库异常:{}",CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return false;
    }
     String filterChar(String strValue){
        String strChar="%%";
        if(strValue.contains(strChar)){
            return  strValue.replace(strChar,"%").trim();
        }else{
            return strValue;
        }
     }
    @Override
    public Boolean batchUpdateStatus(List<RiskMerchantImportUpdateVo> voList) {
        List<String> userIds = voList.stream().map(RiskMerchantImportUpdateVo::getUserId).collect(Collectors.toList());
        Map<Long, RiskMerchantImportUpdateVo> map = voList.stream().collect(Collectors.toMap(bean -> Long.parseLong(bean.getUserId()), bean -> bean));
        List<RiskMerchantManager> riskMerchantManagers = riskMerchantManagerMapper.selectList(new QueryWrapper<RiskMerchantManager>().lambda().in(RiskMerchantManager::getUserId, userIds));
        log.info("::{}::batchUpdateStatus userIds：{},riskMerchantManagers size:{}",CommonUtil.getRequestId(), JSON.toJSON(userIds), riskMerchantManagers.size());
        List<RiskMerchantManager> updateList = new ArrayList<>();
        riskMerchantManagers.forEach(obj -> {
            if (map.containsKey(obj.getUserId())) {
                RiskMerchantImportUpdateVo riskMerchantImportUpdateVo = map.get(obj.getUserId());
                RiskMerchantManager manager = new RiskMerchantManager();
                manager.setId(obj.getId());
                manager.setUserId(Long.valueOf(riskMerchantImportUpdateVo.getUserId()));
                manager.setMerchantOperator(riskMerchantImportUpdateVo.getMerchantOperator());
                manager.setType(getTypeByName(riskMerchantImportUpdateVo.getType()));
                manager.setStatus(getStatusByName(riskMerchantImportUpdateVo.getStatus()));
                manager.setSupplementExplain(riskMerchantImportUpdateVo.getSupplementExplain());
                manager.setUpdateTime(System.currentTimeMillis());
                manager.setProcessTime(System.currentTimeMillis());
                manager.setRecommendTime(System.currentTimeMillis());
                log.info("::{}::batchUpdateStatus：manager参数:{}",CommonUtil.getRequestId(), JSON.toJSON(manager));
                updateList.add(manager);
            }
        });
        log.info("::{}::导入数据：size{}",CommonUtil.getRequestId(), updateList.size());
        return updateBatchById(updateList);
    }


    private static Integer getTypeByName(String type){
        switch (type.trim()) {
            case "投注特征标签":
                return 1;
            case "特殊限额":
                return 2;
            case "特殊延时":
                return 3;
            case "提前结算":
                return 4;
            case "赔率分组":
                return 5;
            case "投注特征预警变更标签":
                return 6;
            case "自动化任务标签变更":
                return 7;
        }
        return 1;
    }

    private static Integer getStatusByName(String status){
        switch (status.trim()) {
            case "同意":
                return 1;
            case "拒绝":
                return 2;
            case "强制执行":
                return 3;
        }
        return 0;
    }

    @Override
    public String getRecommendValue(Integer type, String content, List<StandardSportType> list) {
        if (type.equals(RiskMerchantManagerTypeEnum.Type_3.getCode())) {
            if(StringUtils.isEmpty(content) || content.contains("nulls")){
                return "";
            }
            if (!CollectionUtils.isEmpty(list)) {
                Map<Long, String> map = list.stream().collect(Collectors.toMap(StandardSportType::getId, StandardSportType::getIntroduction));
                if (content.indexOf("[") != -1) {
                    String start = content.substring(0, content.indexOf("["));
                    String types = content.substring(content.indexOf("[") + 1, content.indexOf("]"));
                    String[] strings = types.split(",");
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(start);
                    buffer.append("[");
                    for (int i = 0; i < strings.length; i++) {
                        String name = map.get(Long.valueOf(strings[i]));
                        if(StringUtils.isBlank(name)){
                            continue;
                        }
                        buffer.append(name).append(",");
                    }
                    buffer.setLength(buffer.length() - 1);
                    buffer.append("]");
                    log.info("::{}::content:{},返回：{}",CommonUtil.getRequestId(), content, buffer.toString());
                    return buffer.toString();
                } else {
                    return content;
                }
            }
        } else if (type.equals(RiskMerchantManagerTypeEnum.Type_5.getCode())) {
            switch (content) {
                case "0":
                    return "0";
                case "1":
                    return "A";
                case "2":
                    return "B";
                case "3":
                    return "C";
                case "4":
                    return "D";
                case "11":
                    return "1";
                case "12":
                    return "2";
                case "13":
                    return "3";
                case "14":
                    return "4";
                case "15":
                    return "5";
            }
        }
        return content;
    }


}
