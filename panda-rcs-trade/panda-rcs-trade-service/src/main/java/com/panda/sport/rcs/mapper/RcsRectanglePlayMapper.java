package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.data.rcs.dto.ExtendBean;
import com.panda.sport.rcs.pojo.RcsRectanglePlay;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Component
public interface RcsRectanglePlayMapper extends BaseMapper<RcsRectanglePlay> {

	String queryPlayListByMatch(Map<String, Object> params);

	String queryMatchNoScorePlay(Map<String, Object> params);

	String queryAllUserMatchNoScorePlay(Map<String, Object> params);

	String queryAllMatchNoScorePlay(Map<String, Object> params);

	Map<String, Object> queryMatchInfo(Map<String, Object> params);

	Map<String, Object> queryOutrightMatchInfo(Map<String, Object> params);

	int insertAndUpdate(ExtendBean extendBean);

	void updatePlayHandicapAllOption(ExtendBean extendBean);

}
