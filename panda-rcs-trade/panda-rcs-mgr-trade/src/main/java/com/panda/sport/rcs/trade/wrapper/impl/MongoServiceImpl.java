package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.panda.sport.rcs.trade.wrapper.MongoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-10-29 18:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class MongoServiceImpl<T> implements MongoService<T> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void createCollection(String name) {
        mongoTemplate.createCollection(name);
    }

    @Override
    public String createIndex(String collectionName, String filedName) {
        //配置索引选项
        IndexOptions options = new IndexOptions();
        // 设置为唯一
        options.unique(true);
        //创建按filedName升序排的索引
        return mongoTemplate.getCollection(collectionName).createIndex(Indexes.ascending(filedName), options);
    }


    @Override
    public List<String> getAllIndexes(String collectionName) {
        ListIndexesIterable<Document> list = mongoTemplate.getCollection(collectionName).listIndexes();
        //上面的list不能直接获取size，因此初始化arrayList就不设置初始化大小了
        List<String> indexes = new ArrayList<>();
        for (Document document : list) {
            document.entrySet().forEach((key) -> {
                //提取出索引的名称
                if (key.getKey().equals("name")) {
                    indexes.add(key.getValue().toString());
                }
            });
        }
        return indexes;
    }

    @Override
    public void insert(T info, String collectionName) {
        mongoTemplate.insert(info, collectionName);
    }

    @Override
    public void insertMulti(List<T> infos, String collectionName) {
        mongoTemplate.insert(infos, collectionName);
    }

    @Override
    public void updateById(String id, String collectionName, T info) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        String str = JSON.toJSONString(info);
        JSONObject jQuery = JSON.parseObject(str);
        jQuery.forEach((key, value) -> {
            //因为id相当于传统数据库中的主键，这里使用时就不支持更新，所以需要剔除掉
            if (!key.equals("id")) {
                update.set(key, value);
            }
        });
        mongoTemplate.updateMulti(query, update, info.getClass(), collectionName);
    }

    @Override
    public void update(Map<String, Object> map, String collectionName, T info) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        map.forEach((key, value) -> {
            query.addCriteria(criteria.and(key).is(value));
        });
        Update update = new Update();
        String str = JSON.toJSONString(info);
        JSONObject jQuery = JSON.parseObject(str);
        jQuery.forEach((key, value) -> {
            //因为id相当于传统数据库中的主键，这里使用时就不支持更新，所以需要剔除掉
            if (!key.equals("id")) {
                update.set(key, value);
            }
        });
        mongoTemplate.updateMulti(query, update, info.getClass(), collectionName);
    }

    @Override
    public void save(Map<String, Object> map, String collectionName, T info) {

    }

    @Override
    public boolean exists(Map<String, Object> map, String collectionName, T info) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        map.forEach((key, value) -> {
            query.addCriteria(criteria.and(key).is(value));
        });
        return mongoTemplate.exists(query, info.getClass());
    }

    @Override
    public void upsert(Map<String, Object> map, String collectionName, T info) {
        if (this.exists(map, collectionName, info)) {
            this.update(map, collectionName, info);
        } else {
            this.insert(info, collectionName);
        }
    }

    @Override
    public void deleteById(String id, Class<T> clazz, String collectionName) {
        // 设置查询条件，当id=#{id}
        Query query = new Query(Criteria.where("id").is(id));
        // mongodb在删除对象的时候会判断对象类型，如果你不传入对象类型，只传入了集合名称，它是找不到的
        // 上面我们为了方便管理和提升后续处理的性能，将一个集合限制了一个对象类型，所以需要自行管理一下对象类型
        // 在接口传入时需要同时传入对象类型
        mongoTemplate.remove(query, clazz, collectionName);
    }

    @Override
    public T selectById(String id, Class<T> clazz, String collectionName) {
        // 查询对象的时候，不仅需要传入id这个唯一键，还需要传入对象的类型，以及集合的名称
        return mongoTemplate.findById(id, clazz, collectionName);
    }

    @Override
    public List<T> selectList(String collectName, Class<T> clazz) {
        return selectList(collectName, clazz, null, null);
    }

    @Override
    public List<T> selectList(String collectName, Class<T> clazz, Integer currentPage, Integer pageSize) {
        //设置分页参数
        Query query = new Query();
        //设置分页信息
        if (!ObjectUtils.isEmpty(currentPage) && ObjectUtils.isEmpty(pageSize)) {
            query.limit(pageSize);
            query.skip(pageSize * (currentPage - 1));
        }
        return mongoTemplate.find(query, clazz, collectName);
    }


    @Override
    public List<T> selectByCondition(String collectName, Map<String, String> conditions, Class<T> clazz, Integer currentPage, Integer pageSize) {
        if (ObjectUtils.isEmpty(conditions)) {
            return selectList(collectName, clazz, currentPage, pageSize);
        } else {
            //设置分页参数
            Query query = new Query();
            query.limit(pageSize);
            query.skip(currentPage);
            // 往query中注入查询条件
            conditions.forEach((key, value) -> query.addCriteria(Criteria.where(key).is(value)));
            return mongoTemplate.find(query, clazz, collectName);
        }
    }
}
