package com.panda.sport.rcs.vo.statistics;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.io.*;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.websocket.bean
 * @Description :  期望详情
 * @Date: 2020-01-11 17:50
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitDetailBean extends RcsBaseEntity<ProfitDetailBean> implements Serializable {
    /**
     * 期望值
     */
    private List<MarketProfitVo> rcsProfitRectangleList;

    /**
     * 平衡值
     */
    private  List<MarketBalanceVo> balancesList;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 玩法id
     */
    private Integer playId;

    /**
     * 分数
     */
    private Integer score;

    /**
     * @Description   深拷贝
     * @Param []
     * @Author  myname
     * @Date  14:42 2020/1/13
     * @return java.lang.Object
     **/
    public ProfitDetailBean deepCopy() throws Exception
    {
        // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。
        // 所以利用这个特性可以实现对象的深拷贝。
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(this);

        // 将流序列化成对象
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(bis);

        return (ProfitDetailBean)ois.readObject();
    }
}
