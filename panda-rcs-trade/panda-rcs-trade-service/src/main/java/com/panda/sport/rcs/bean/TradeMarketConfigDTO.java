package com.panda.sport.rcs.bean;


import java.io.Serializable;

import lombok.Data;

/**
 * <pre>
 *  附加字段使用说明：
 *    场景：联赛下某一玩法关盘
 *    对应的配置：
 *    level    targetId     addition1      addition2       addition3
 *    1          玩法ID       联赛ID
 *
 *    场景：按玩法关盘
 *    对应的配置：
 *    level    targetId     addition1      addition2       addition3
 *    1          玩法ID
 *
 *    场景：按联赛关盘
 *    对应的配置：
 *    level    targetId     addition1      addition2       addition3
 *    2          联赛ID
 *
 *    场景：按赛事关盘
 *    对应的配置：
 *    level    targetId     addition1      addition2       addition3
 *    3          赛事ID
 *
 *    场景：按盘口关盘
 *    对应的配置：
 *    level    targetId     addition1      addition2       addition3
 *    4          盘口ID
 *    场景：三方数据源进行赛事级别的开关封
 *    level    targetId     sourceSystem	addition1      addition2       addition3
 *    3		   三方赛事源ID 	3			数据源编码     运动种类ID
 * </pre>
 * @Description  :  操盘配置DTO
 * @author       :  Vito
 * @Date:  2019年11月6日 下午2:22:06
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TradeMarketConfigDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 配置ID，由配置服务消费者生成
	 */
	private String configId;

	/**
	 * 配置生效级别:
	 * 0：全部
	 * 1：玩法
	 * 2：联赛
	 * 3：赛事
	 * 4：盘口
	 */
	private Integer level;

	/**
	 * 配置生效的目标数据ID。与level配合使用。比如为标准联赛ID，标准赛事ID，标准盘口ID
	 * 如果是全部，targetId传0
	 */
	private String targetId;

	/**
	 * 操盘类型
	 * 0:自动操盘
	 * 1:手动操盘
	 * null 表示不修改当前操盘类型
	 */
	private Integer tradeType;

	/**
	 * 盘口状态：
	 *   0:active 开,
	 *   1:suspended 封,
	 *   2:deactivated 关,
	 *   11:锁
	 * null 表示不修改当前盘口状态
	 */
	private Integer marketStatus;

	/**
	 * 配置来源：
	 *  1：赛程管理
	 *  2：操盘系统
	 *  3: 三方数据源
	 */
	private Integer sourceSystem;

	/**
	 * 当sourceSystem为3时传值 data_source_code
	 * 附加字段1
	 */
	private String addition1;

	/**
	 * 当sourceSystem为3时传值 sport_id
	 */
	private String addition2;

	/**
	 * 附加字段3
	 */
	private String addition3;

	/**
	 * 配置是否激活
	 *  0： 未激活
	 *  1：已激活
	 */
	private Integer active;

	/**
	 * 配置修改人
	 */
	private Long operaterId;

	/**
	 * 配置修改时间
	 */
	private Long modifyTime;
}
