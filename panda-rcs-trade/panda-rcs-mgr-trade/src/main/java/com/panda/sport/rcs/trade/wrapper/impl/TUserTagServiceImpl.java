package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.TUserLevelMapper;
import com.panda.sport.rcs.mapper.TUserTagMapper;
import com.panda.sport.rcs.pojo.TUserLevel;
import com.panda.sport.rcs.pojo.TUserTag;
import com.panda.sport.rcs.trade.wrapper.TUserLevelService;
import com.panda.sport.rcs.trade.wrapper.TUserTagService;
import org.springframework.stereotype.Service;

/**
 * @program: xindaima
 * @description:
 * @author: Kir
 * @create: 2021-04-09 14:06
 **/
@Service
public class TUserTagServiceImpl extends ServiceImpl<TUserTagMapper, TUserTag> implements TUserTagService {
}
