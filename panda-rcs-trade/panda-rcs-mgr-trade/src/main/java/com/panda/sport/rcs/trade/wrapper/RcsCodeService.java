package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsCode;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
public interface RcsCodeService extends IService<RcsCode> {
    /**
     * @return com.panda.sport.rcs.pojo.RcsCode
     * @Description /增加数据字典
     * @Param [rcsCode]
     * @Author kimi
     * @Date 2019/10/5
     **/

    RcsCode addRcsCode(RcsCode rcsCode);

    /**
     * @return com.panda.sport.rcs.pojo.RcsCode
     * @Description /删除数据字典
     * @Param [rcsCode]
     * @Author kimi
     * @Date 2019/10/5
     **/
    void deleteRcsCode(RcsCode rcsCode);

    /**
     * @return com.panda.sport.rcs.pojo.RcsCode
     * @Description /更新数据字典
     * @Param [rcsCode]
     * @Author kimi
     * @Date 2019/10/5
     **/
    RcsCode updateRcsCode(RcsCode rcsCode);

    /**
     * @Description 只能查一条数据   一级目录只有一条可以查  childKey传入空
     * @Param [rcsCode]     一级目录+二级目录 可以查
     * @Author kimi
     * @Date 2019/10/5
     **/
    Long getRcsCodeList(String fatherKey, String childKey);

    /**
     * @Description //直接从数据库拿
     * @Author kimi
     * @Date 2019/10/12
     **/

    List<RcsCode> getRcsCodeList(Map<String, Object> columnMap);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsCode>
     * @Description //获取商户列表
     * @Param [columnMap]
     * @Author kimi
     * @Date 2019/10/5
     **/
    List<RcsCode> getBusinessList();

    RcsCode getBusiness(Long id);

    String getValue(String fatherKey, String childKey);

    /**
     * 根据主key获取字典集合
     * @param fatherKey
     * @return
     */
    List<RcsCode> selectRcsCods(String fatherKey);

    /**
     * 根据主key获取字典集合
     * @param fatherKey
     * @param childKey
     * @return
     */
    List<RcsCode> selectRcsCods(String fatherKey,String childKey);

    /**
     * @return java.util.List<java.lang.String>
     * @Description //TODO
     * @Param [fatherKey, playIds]
     * @Author kimi
     * @Date 2020/2/18
     **/
    List<String> selectPlayIdsByList(List<Long> stateIds);

    /**
     * @return java.util.List<java.lang.String>
     * @Description //TODO
     * @Param [playIds]
     * @Author kimi
     * @Date 2020/3/5
     **/
    List<String> selectPlayIds(Long stateId);
}
