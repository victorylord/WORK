package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.RcsCodeMapper;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.panda.sport.rcs.constants.RedisKeys.CATEGORY_PHASE_CACHE;
import static com.panda.sport.rcs.constants.RedisKeys.EXP_TIME;

/**
 * <p>
 * 服务实现类
 * </p>
 * 数据字典接口
 * 、、
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public class RcsCodeServiceImpl extends ServiceImpl<RcsCodeMapper, RcsCode> implements RcsCodeService {
    @Autowired
    private RcsCodeMapper rcsCodeMapper;
    @Autowired
    private RedisClient redisClient;

    @Override
    public RcsCode addRcsCode(RcsCode rcsCode) {
        int insert = rcsCodeMapper.insert(rcsCode);
        if (rcsCode.getStatus() == 1) {
            redisClient.hSet(RedisKeys.RCS_CODE, getHkey(rcsCode), rcsCode.getValue().toString());
        }
        rcsCode.setId(insert);
        return rcsCode;
    }

    @Override
    public void deleteRcsCode(RcsCode rcsCode) {
        redisClient.hashRemove(RedisKeys.RCS_CODE, getHkey(rcsCode));
        rcsCodeMapper.deleteById(rcsCode.getId());
    }

    @Override
    public RcsCode updateRcsCode(RcsCode rcsCode) {
        redisClient.hSet(RedisKeys.RCS_CODE, getHkey(rcsCode), rcsCode.getValue().toString());
        rcsCodeMapper.updateById(rcsCode);
        return rcsCode;
    }

    /**
     * @return int
     * @Description //todo 只能查一条具体数据 准确查得用下面这个方法
     * @Param [fatherKey, childKey]
     * @Author kimi
     * @Date 2019/10/7
     **/

    @Override
    public Long getRcsCodeList(String fatherKey, String childKey) {
        //从缓存拿
        String keys = fatherKey;
        if (childKey != null) {
            keys = fatherKey + childKey;
        }
        if (redisClient.hexists(RedisKeys.RCS_CODE, keys)) {
            return Long.parseLong(redisClient.hGet(RedisKeys.RCS_CODE, keys));
        } else {
            //缓存里面没有 从数据库取出
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("father_key", fatherKey);
            if (childKey != null) {
                columnMap.put("child_key", childKey);
            }
            List<RcsCode> rcsCodeList = getRcsCodeList(columnMap);
            //缓存也没有
            if (rcsCodeList.size() == 0) {
                return null;
            } else {
                if (rcsCodeList.size() == 1) {
                    String value = rcsCodeList.get(0).getValue();
                    redisClient.hSet(RedisKeys.RCS_CODE, getHkey(rcsCodeList.get(0)), value);
                    return Long.parseLong(value);
                } else {
                    log.warn("取出了多条数据");
                    return null;
                }
            }
        }
    }

    @Override
    public List<RcsCode> getRcsCodeList(Map<String, Object> columnMap) {
        //直接从数据库拿
        columnMap.put("status", 1);
        return rcsCodeMapper.selectByMap(columnMap);
    }

    @Override
    public List<RcsCode> getBusinessList() {
        LambdaQueryWrapper<RcsCode> lambdaQueryWrapper = new QueryWrapper<RcsCode>().lambda();
        lambdaQueryWrapper.eq(RcsCode::getFatherKey,  "business");
        lambdaQueryWrapper.eq(RcsCode::getStatus,  1);
        lambdaQueryWrapper.select(RcsCode::getValue,RcsCode::getChildKey,RcsCode::getFatherKey,RcsCode::getId,RcsCode::getStatus);
        List<RcsCode> codes = this.list(lambdaQueryWrapper);
        return codes;
    }

    @Override
    public RcsCode getBusiness(Long id) {
        QueryWrapper<RcsCode> wrapper = new QueryWrapper<>();
        wrapper.eq("father_key", "business");
        wrapper.eq("value", id);
        wrapper.eq("status", 1);
        return rcsCodeMapper.selectOne(wrapper);
    }

    @Override
    public String getValue(String fatherKey, String childKey) {
        String result = "";
        String key = String.format(RedisKeys.RCS_CODE_CACHE, fatherKey, childKey);
        boolean exist = redisClient.exist(key);
        if (exist) {
            result = redisClient.get(key);
        } else {
            QueryWrapper<RcsCode> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(RcsCode::getFatherKey, fatherKey);
            wrapper.lambda().eq(RcsCode::getChildKey, childKey);
            RcsCode rcsCode = rcsCodeMapper.selectOne(wrapper);
            if (rcsCode != null) {
                result = rcsCode.getValue();
                redisClient.setExpiry(key, rcsCode.getValue(), 1000 * 60 * 60 * 24L);
            }
        }
        return result;
    }

    @PostConstruct
    private void load() {
        //数据字典数据全部拉到缓存里面
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("status", 1);
        List<RcsCode> rcsCodes = rcsCodeMapper.selectByMap(columnMap);
        if (rcsCodes.size() > 0) {
            for (RcsCode rcsCode : rcsCodes) {
                redisClient.hSet(RedisKeys.RCS_CODE, getHkey(rcsCode), rcsCode.getValue());
            }
        }

        List<RcsCode> ballPhase = selectRcsCods("play_phase");
        if(ballPhase.size()>0){
            redisClient.setExpiry(CATEGORY_PHASE_CACHE, JsonFormatUtils.toJson(ballPhase), EXP_TIME);
        }


    }

    /**
     * @return java.lang.String
     * @Description 缓存的key值
     * @Param [rcsCode]
     * @Author kimi
     * @Date 2019/10/7
     **/
    private String getHkey(RcsCode rcsCode) {
        if (rcsCode.getChildKey() == null) {
            return rcsCode.getFatherKey();
        } else {
            return rcsCode.getFatherKey() + rcsCode.getChildKey();
        }
    }

    @Override
    public List<RcsCode> selectRcsCods(String fatherKey) {
        QueryWrapper<RcsCode> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsCode::getFatherKey, fatherKey);
        List<RcsCode> rcsCodes = rcsCodeMapper.selectList(wrapper);
        return rcsCodes;
    }

    @Override
    public List<RcsCode> selectRcsCods(String fatherKey,String childKey) {
        QueryWrapper<RcsCode> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsCode::getFatherKey, fatherKey);
        wrapper.lambda().eq(RcsCode::getChildKey, childKey);
        List<RcsCode> rcsCodes = rcsCodeMapper.selectList(wrapper);
        return rcsCodes;
    }

    @Override
    public List<String> selectPlayIdsByList(List<Long> stateIds) {
        return rcsCodeMapper.selectPlayIdsByList(stateIds);
    }

    @Override
    public List<String> selectPlayIds(Long stateIds) {
        List<Long> list = new ArrayList<>();
        list.add(stateIds);
        return selectPlayIdsByList(list);
    }
}
