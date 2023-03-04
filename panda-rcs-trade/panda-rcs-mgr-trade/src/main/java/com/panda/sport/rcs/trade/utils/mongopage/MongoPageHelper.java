package com.panda.sport.rcs.trade.utils.mongopage;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MongoDB分页查询工具类.
 *
 * @author Ryan Miao at 2018-06-07 14:46
 **/
@Component
@Slf4j
public class MongoPageHelper {

    public static final int FIRST_PAGE_NUM = 1;
    public static final String ID = "_id";
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoPageHelper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * 分页查询，直接返回集合类型的结果.
     *
     * @see MongoPageHelper#pageQuery(org.springframework.data.mongodb.core.query.Query,
     * Class, Function, Integer, Integer,
     * String)
     */
    public <T> PageResult<T> pageQuery(Query query, Class<T> entityClass, Integer pageSize,
                                       Integer pageNum) {
        return pageQuery(query, entityClass, Function.identity(), pageSize, pageNum, null);
    }

    /**
     * 聚合分页查询，直接返回集合类型的结果.
     *
     * @see MongoPageHelper#pageQuery(org.springframework.data.mongodb.core.query.Query,
     * Class, Function, Integer, Integer,
     * String)
     */
    public <T> PageResult<T> pageAggregationQuery(List<AggregationOperation> commonOperations, String collecttionNmae, Class<T> entityClass, Integer pageSize,
                                                  Integer pageNum) {

        List<T> allResults = mongoTemplate.aggregate(Aggregation.newAggregation(commonOperations), collecttionNmae, entityClass).getMappedResults();
        log.info("赛事列表MONGO查询条件"+Aggregation.newAggregation(commonOperations).toString());
        //分页逻辑
        final PageResult<T> pageResult = new PageResult<>();
        if (allResults.size() > 0) {
            long total = allResults.size();
            final Integer pages = (int) Math.ceil(total / (double) pageSize);
            if (pageNum <= 0 || pageNum > pages) {
                pageNum = FIRST_PAGE_NUM;
            }
            commonOperations.add(Aggregation.skip(pageSize * (pageNum - 1)));
            commonOperations.add(Aggregation.limit(pageSize));
            Aggregation aggregation = Aggregation.newAggregation(commonOperations);

            List<T> mappedResults = mongoTemplate.aggregate(aggregation, collecttionNmae, entityClass).getMappedResults();
            pageResult.setTotal(total);
            pageResult.setPages(pages);
            pageResult.setPageSize(pageSize);
            pageResult.setPageNum(pageNum);
            pageResult.setList(mappedResults);
        }else{
            pageResult.setList(new ArrayList<>());
        }

        return pageResult;
    }

    /**
     * 分页查询，不考虑条件分页，直接使用skip-limit来分页.
     *
     * @see MongoPageHelper#pageQuery(org.springframework.data.mongodb.core.query.Query,
     * Class, Function, Integer, Integer,
     * String)
     */
    public <T, R> PageResult<R> pageQuery(Query query, Class<T> entityClass, Function<T, R> mapper,
                                          Integer pageSize, Integer pageNum) {
        return pageQuery(query, entityClass, mapper, pageSize, pageNum, null);
    }

    /**
     * 分页查询.
     *
     * @param query       Mongo Query对象，构造你自己的查询条件.
     * @param entityClass Mongo collection定义的entity class，用来确定查询哪个集合.
     * @param mapper      映射器，你从db查出来的list的元素类型是entityClass, 如果你想要转换成另一个对象，比如去掉敏感字段等，可以使用mapper来决定如何转换.
     * @param pageSize    分页的大小.
     * @param pageNum     当前页.
     * @param lastId      条件分页参数, 区别于skip-limit，采用find(_id>lastId).limit分页.
     *                    如果不跳页，像朋友圈，微博这样下拉刷新的分页需求，需要传递上一页的最后一条记录的ObjectId。 如果是null，则返回pageNum那一页.
     * @param <T>         collection定义的class类型.
     * @param <R>         最终返回时，展现给页面时的一条记录的类型。
     * @return PageResult，一个封装page信息的对象.
     */
    public <T, R> PageResult<R> pageQuery(Query query, Class<T> entityClass, Function<T, R> mapper,
                                          Integer pageSize, Integer pageNum, String lastId) {
        //分页逻辑
        long total = mongoTemplate.count(query, entityClass);
        final Integer pages = (int) Math.ceil(total / (double) pageSize);
        if (pageNum <= 0 || pageNum > pages) {
            pageNum = FIRST_PAGE_NUM;
        }
        final Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(lastId)) {
            if (pageNum != FIRST_PAGE_NUM) {
                criteria.andOperator(Criteria.where(ID).gt(new ObjectId(lastId)));
                //criteria.and(ID).gt(new ObjectId(lastId));
            }
            query.limit(pageSize);
        } else {
            int skip = pageSize * (pageNum - 1);
            query.skip(skip).limit(pageSize);
        }
        final List<T> entityList = mongoTemplate.find(query, entityClass);
        final PageResult<R> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setPages(pages);
        pageResult.setPageSize(pageSize);
        pageResult.setPageNum(pageNum);
        pageResult.setList(entityList.stream().map(mapper).collect(Collectors.toList()));
        return pageResult;
    }

}