package com.panda.sport.rcs.trade.wrapper.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.panda.sport.rcs.mapper.RcsTradingAssignmentMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchCollectionMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.dto.QueryPreLiveMatchDto;
import com.panda.sport.rcs.trade.wrapper.MongoService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchCollectionService;
import com.panda.sport.rcs.utils.CollectionUtil;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-10-25 14:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchCollectionServiceImpl extends ServiceImpl<RcsMatchCollectionMapper, RcsMatchCollection> implements RcsMatchCollectionService {
    @Autowired
    private RcsMatchCollectionMapper rcsMatchCollectionMapper;

    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    private RcsTradingAssignmentMapper tradingAssignmentMapper;

    @Autowired
    MongoService mongoService;

    @Override
    public List<RcsMatchCollection> selectByMap(Map<String, Object> columnMap) {
        return rcsMatchCollectionMapper.selectByMap(columnMap);
    }

    @Override
    public boolean exist(Long matchId, Long tournamentId, Long tradeId) {
        QueryWrapper<RcsMatchCollection> queryWrapper = new QueryWrapper();
        if (matchId != null && tournamentId != null) {
            queryWrapper.lambda().eq(RcsMatchCollection::getType, 1)
                    .eq(RcsMatchCollection::getMatchId, matchId)
                    .eq(RcsMatchCollection::getUserId, tradeId);
            RcsMatchCollection matchColl = baseMapper.selectOne(queryWrapper);
            if (null != matchColl) {
                return matchColl.getStatus() == 1 ? true : false;
            } else {
                QueryWrapper<RcsMatchCollection> queryTour = new QueryWrapper();
                queryTour.lambda().eq(RcsMatchCollection::getType, 2)
                        .eq(RcsMatchCollection::getStatus, 1)
                        .eq(RcsMatchCollection::getTournamentId, tournamentId)
                        .eq(RcsMatchCollection::getUserId, tradeId);
                List<RcsMatchCollection> tournamentColl = baseMapper.selectList(queryTour);
                if (!CollectionUtils.isEmpty(tournamentColl)) {
                    return true;
                }
            }
        } else if (tournamentId != null) {
            queryWrapper.lambda().eq(RcsMatchCollection::getType, 2).eq(RcsMatchCollection::getTournamentId, tournamentId).eq(RcsMatchCollection::getUserId, tradeId).eq(RcsMatchCollection::getStatus, 1);
            List<RcsMatchCollection> rcsMatchCollections = baseMapper.selectList(queryWrapper);
            if (rcsMatchCollections.size() > 0) return true;
        }
        return false;
    }

    @Override
    public List<QueryPreLiveMatchDto> existList(List<QueryPreLiveMatchDto> matchDtos, Long tradeId, String marketType) {
        if (!CollectionUtils.isEmpty(matchDtos)) {
            List<Long> matchIds = matchDtos.stream().map(map -> map.getMatchId()).collect(Collectors.toList());

            Integer matchType = marketType.equals("PRE")?0:1;
            List<Long> tradeMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(tradeId), matchType);
            QueryWrapper<RcsMatchCollection> queryWrapper = new QueryWrapper();
            queryWrapper.lambda().eq(RcsMatchCollection::getType, 1)
                    .eq(RcsMatchCollection::getUserId, tradeId)
                    .in(RcsMatchCollection::getMatchId, matchIds);
            List<RcsMatchCollection> matchCollections = baseMapper.selectList(queryWrapper);
            Map<Long, Integer> matchMap = null;
            if (!CollectionUtils.isEmpty(matchCollections)) {
                matchMap = matchCollections.stream().collect(Collectors.toMap(RcsMatchCollection::getMatchId, RcsMatchCollection::getStatus, (v1, v2) -> v1));
            }
            List<Long> tournameIds = matchDtos.stream().map(map -> map.getTournamentId()).collect(Collectors.toList());

            QueryWrapper<RcsMatchCollection> tourWrapper = new QueryWrapper();
            tourWrapper.lambda().eq(RcsMatchCollection::getType, 2)
                    .eq(RcsMatchCollection::getUserId, tradeId)
                    .in(RcsMatchCollection::getTournamentId, tournameIds);

            List<RcsMatchCollection> tourCollections = baseMapper.selectList(tourWrapper);
            Map<Long, Integer> tourMap = null;
            if (!CollectionUtils.isEmpty(tourCollections)) {
                tourMap = tourCollections.stream().filter(distinctByKey(b -> b.getTournamentId())).collect(Collectors.toMap(RcsMatchCollection::getTournamentId, RcsMatchCollection::getStatus));
            }
            for (QueryPreLiveMatchDto dto : matchDtos) {
                Long matchId = dto.getMatchId();
                Long tournamentId = dto.getTournamentId();
                String userId = "";
                Integer matchStatus = null;
                if ("PRE".equals(marketType)) {
                    userId = dto.getPreTraderId();
                } else if ("LIVE".equals(marketType)) {
                    userId = dto.getLiveTraderId();
                }
                if ("End".equals(dto.getStatus())) {
                    dto.setFavoriteStatus(2);
                    dto.setTournamentFavoriteStatus(2);
                } else {
                    if ((StringUtils.isNotBlank(userId) && userId.equals(String.valueOf(tradeId)))||
                            (!CollectionUtils.isEmpty(tradeMatchIds)&&tradeMatchIds.contains(matchId))) {
                        matchStatus = 0;
                    } else {
                        if (!CollectionUtils.isEmpty(matchMap))
                            matchStatus = matchMap.get(matchId);
                        if (matchStatus == null) {
                            if (!CollectionUtils.isEmpty(tourMap)) {
                                matchStatus = tourMap.get(tournamentId) == null ? 2 : tourMap.get(tournamentId) == 1 ? 1 : 2;
                            } else {
                                matchStatus = 2;
                            }
                        } else {
                            matchStatus = matchStatus == 1 ? 1 : 2;
                        }
                    }
                    dto.setFavoriteStatus(matchStatus);

                    if (CollectionUtils.isEmpty(tourMap)) {
                        dto.setTournamentFavoriteStatus(2);
                    } else {
                        Integer tourStatus = tourMap.get(tournamentId);
                        dto.setTournamentFavoriteStatus(tourStatus == null ? 2 : tourStatus == 1 ? 1 : 2);
                    }
                }
            }
        }
        return matchDtos;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public Integer selectMatchCollectionCount(RcsMatchCollection matchCollection) {
        if (matchCollection.getUserId() == null) return null;
        List<RcsMatchCollection> rcsMatchCollections1 = rcsMatchCollectionMapper.selectListTotournament(matchCollection);
        List<RcsMatchCollection> rcsMatchCollections = rcsMatchCollectionMapper.selectListToCount(matchCollection);
        List<Long> matchIds = new ArrayList<>();
        List<Long> noMatchIds = new ArrayList<>();
        List<Long> tournamentIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rcsMatchCollections)) {
            for (RcsMatchCollection rcsMatchCollection : rcsMatchCollections) {
                if (rcsMatchCollection.getStatus() == 1) {
                    matchIds.add(rcsMatchCollection.getMatchId());
                }
                noMatchIds.add(rcsMatchCollection.getMatchId());
            }
        }
        if (!CollectionUtils.isEmpty(rcsMatchCollections1)) {
            for (RcsMatchCollection rcsMatchCollection : rcsMatchCollections1) {
                tournamentIds.add(rcsMatchCollection.getTournamentId());
            }
        }
        List<StandardMatchInfo> standardMatchInfos;
        if (!CollectionUtils.isEmpty(tournamentIds)) {
            standardMatchInfos = standardMatchInfoMapper.selectTournamentCount(tournamentIds, noMatchIds, matchCollection.getMatchType());
            if (CollectionUtils.isEmpty(standardMatchInfos)) {
                return matchIds.size();
            } else {
                return matchIds.size() + standardMatchInfos.size();
            }
        } else {
            return matchIds.size();
        }
    }

    @Override
    public boolean updateRcsMatchCollection(RcsMatchCollection matchCollection) {
        try {
        	// 如果是赛事
            if (matchCollection.getType() == 1) {
                StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchCollection.getMatchId());
                if (standardMatchInfo != null) {
                    matchCollection.setTournamentId(standardMatchInfo.getStandardTournamentId());
                    matchCollection.setSportId(standardMatchInfo.getSportId());
                } else {
                    throw new RcsServiceException("赛事不存在");
                }
                savaOrUpdate(matchCollection);
                // 如果是联赛
            } else if (matchCollection.getType() == 2){
                StandardSportTournament standardSportTournament = standardSportTournamentMapper.selectById(matchCollection.getTournamentId());
                matchCollection.setSportId(standardSportTournament.getSportId());
                matchCollection.setMatchId(0L);
                savaOrUpdate(matchCollection);
	            Long time = DateUtils.stringToDateAddTwelveHour(DateUtils.getDateExpect(System.currentTimeMillis()));
	            List<StandardMatchInfo> standardMatchInfos = standardMatchInfoMapper.selectOpenStatusMatchByTournament(matchCollection.getTournamentId(), time);
	            List<RcsMatchCollection> rcsMatchCollections = new ArrayList<>();
	            for (StandardMatchInfo standardMatchInfo : standardMatchInfos) {
	                RcsMatchCollection rcsMatchCollection = BeanCopyUtils.deepCopyProperties(matchCollection, RcsMatchCollection.class);
	                rcsMatchCollection.setMatchId(standardMatchInfo.getId());
	                rcsMatchCollection.setType(1);
	                rcsMatchCollection.setSportId(standardMatchInfo.getSportId());
	                rcsMatchCollection.setStatus(matchCollection.getStatus());
	                rcsMatchCollections.add(rcsMatchCollection);
	            }
	            if (!CollectionUtils.isEmpty(rcsMatchCollections)) {
	            	for (RcsMatchCollection rcsMatchCollection : rcsMatchCollections) {
	            		savaOrUpdate(rcsMatchCollection);
					}
	            }
            }else {
            	throw new RcsServiceException("收藏更新异常，不存在的收藏类型");
            }
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  根据  userId,MatchId,Type确认是否存在，不存在插入数据，存在更新
     * 
     * @param matchCollection
     */
    private void savaOrUpdate(RcsMatchCollection matchCollection) {
    	QueryWrapper<RcsMatchCollection> queryWrapper = new QueryWrapper<RcsMatchCollection>();
  		queryWrapper.lambda().eq(RcsMatchCollection :: getMatchId , matchCollection.getMatchId());
  		queryWrapper.lambda().eq(RcsMatchCollection :: getType , matchCollection.getType());
  		queryWrapper.lambda().eq(RcsMatchCollection :: getTournamentId , matchCollection.getTournamentId());
  		queryWrapper.lambda().eq(RcsMatchCollection :: getUserId , matchCollection.getUserId()).last("LIMIT 1");;
  		RcsMatchCollection selectOneType = baseMapper.selectOne(queryWrapper);
  		Date date = new Date();
  		// 不存在就插入
  		if (selectOneType == null) {
  			matchCollection.setId(null);
	        matchCollection.setCreateTime(date);
  			baseMapper.insert(matchCollection);
  		}
  		// 存在就更新
  		else {
	        matchCollection.setUpdateTime(date);
  			if(matchCollection.getType()==1) {
	  			baseMapper.updateByMatchId(matchCollection);
  			}else if(matchCollection.getType()==2) {
  				baseMapper.updateByTournamentId(matchCollection);
  			}
  		}
	}

	@Override
    public List<RcsMatchCollection> getSyUserColletCondition(RcsMatchCollection rcsMatchCollection1) {
        if (rcsMatchCollection1.getUserId() == null) return null;
        List<RcsMatchCollection> rcsMatchCollections1 = rcsMatchCollectionMapper.selectListTotournament(rcsMatchCollection1);
        List<RcsMatchCollection> rcsMatchCollections = rcsMatchCollectionMapper.selectListToCount(rcsMatchCollection1);
        List<Long> matchIds = new ArrayList<>();
        List<Long> noMatchIds = new ArrayList<>();
        List<Long> tournamentIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rcsMatchCollections)) {
            for (RcsMatchCollection rcsMatchCollection : rcsMatchCollections) {
                if (rcsMatchCollection.getStatus() == 1) {
                    matchIds.add(rcsMatchCollection.getMatchId());
                }
                noMatchIds.add(rcsMatchCollection.getMatchId());
            }
        }
        if (!CollectionUtils.isEmpty(rcsMatchCollections1)) {
            for (RcsMatchCollection rcsMatchCollection : rcsMatchCollections1) {
                tournamentIds.add(rcsMatchCollection.getTournamentId());
            }
        }
        List<RcsMatchCollection> rcsMatchCollections3 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tournamentIds)) {
            CollectionUtil.removeDuplicate(tournamentIds);
            List<StandardMatchInfo> standardMatchInfos = standardMatchInfoMapper.selectTournamentCount(tournamentIds, noMatchIds, rcsMatchCollection1.getMatchType());
            for (StandardMatchInfo standardMatchInfo : standardMatchInfos) {
                RcsMatchCollection rcsMatchCollection = new RcsMatchCollection();
                rcsMatchCollection.setMatchId(standardMatchInfo.getId());
                rcsMatchCollections3.add(rcsMatchCollection);
            }
        }
        for (Long matchId : matchIds) {
            RcsMatchCollection rcsMatchCollection = new RcsMatchCollection();
            rcsMatchCollection.setMatchId(matchId);
            rcsMatchCollections3.add(rcsMatchCollection);
        }
        return rcsMatchCollections3;
    }

    @Override
    public Integer queryFavoriteStatus(Long userId, Long matchId, Long beginTime) {
        Integer count = rcsMatchCollectionMapper.queryFavoriteStatus(userId, matchId, beginTime);
        if (ObjectUtils.isEmpty(count)) {
            count = NumberUtils.INTEGER_TWO;
        }
        return count;
    }

    @Override
    public List<Long> queryCollMatchIds(RcsMatchCollection collection,List<Long> traderMatchIds) {
        List<Long> result = new ArrayList<>();
        Long userId = collection.getUserId();
        if (userId == null) return result;

        //查询联赛收藏
        List<Long> tournamentIds = new ArrayList<>();
        List<RcsMatchCollection> tourColl = rcsMatchCollectionMapper.selectListTotournament(collection);
        if (!CollectionUtils.isEmpty(tourColl)) {
            tournamentIds = tourColl.stream().filter(filter -> null != filter.getTournamentId()).map(map -> map.getTournamentId()).collect(Collectors.toList());
        }
        //查询赛事收藏
        List<Long> matchIds = new ArrayList<>();
        List<Long> noMatchIds = new ArrayList<>();
        List<RcsMatchCollection> matchColl = rcsMatchCollectionMapper.queryMatchColls(collection);
        if (!CollectionUtils.isEmpty(matchColl)) {
            matchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() == 1).map(map -> map.getMatchId()).collect(Collectors.toList());
            noMatchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() != 1).map(map -> map.getMatchId()).collect(Collectors.toList());
        }
        if (!CollectionUtils.isEmpty(tournamentIds)) {
            //查询联赛下仅自己收藏的赛事
            result = standardMatchInfoMapper.selectMatchIds(tournamentIds, noMatchIds, String.valueOf(userId), collection.getBeginTime());
        }
        if(!CollectionUtils.isEmpty(traderMatchIds))result.addAll(traderMatchIds);
        result.addAll(matchIds);
        CollectionUtil.removeDuplicate(result);
        return result;
    }

    @Override
    public List<Long> querytourColl(RcsMatchCollection collection) {
        List<Long> tournamentIds = new ArrayList<>();
        List<RcsMatchCollection> tourColl = rcsMatchCollectionMapper.selectListTotournament(collection);
        if (!CollectionUtils.isEmpty(tourColl)) {
            tournamentIds = tourColl.stream().filter(filter -> null != filter.getTournamentId()).map(map -> map.getTournamentId()).collect(Collectors.toList());
        }
        return tournamentIds;
    }

    @Override
    public List<Long> queryNoMatchIds(RcsMatchCollection collection) {
        List<Long> noMatchIds = new ArrayList<>();
        List<RcsMatchCollection> matchColl = rcsMatchCollectionMapper.queryMatchColls(collection);
        if (!CollectionUtils.isEmpty(matchColl)) {
            noMatchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() != 1).map(map -> map.getMatchId()).collect(Collectors.toList());
        }
        return noMatchIds;
    }


}
