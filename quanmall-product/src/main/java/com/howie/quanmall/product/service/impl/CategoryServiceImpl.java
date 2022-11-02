package com.howie.quanmall.product.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.howie.quanmall.product.service.CategoryBrandRelationService;
import com.howie.quanmall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.product.dao.CategoryDao;
import com.howie.quanmall.product.entity.CategoryEntity;
import com.howie.quanmall.product.service.CategoryService;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        List<CategoryEntity> level1Menus = entities.stream().
                filter(categoryEntity -> categoryEntity.getParentCid() == 0
                ).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO: 检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    /*@Caching(evict = {
            @CacheEvict(value = "category", key = "'level1Categorys'"),
            @CacheEvict(value = "category", key = "'getCatalogJson'")
    })*/
    @CacheEvict(value = "category",allEntries = true)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = "category", key = "'level1Categorys'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //根据当前二级分类找三级分类
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(collect);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return parent_cid;
    }

    // TODO: 2022/1/27 产生堆外内存溢出 OutOfDirectMemoryError
    //springboot2.0以后默认使用lettuce作为操作redis的客户端。使用netty进行网络通信。
    //lettuce的bug导致netty堆外内存溢出。 netty如果没有指定堆外内存，默认使用-Xmx。
    //可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案：不能使用-Dio.netty.maxDirectMemory只去调大内存，只是延缓。
    //1、升级lettuce（底层使用netty，吞吐量大）
    //2、切换使用jedis（很久没更新）
    @Deprecated
    public Map<String, List<Catalog2Vo>> getCatalogJson1() {
        //缓存中存的数据是json字符串
        //json跨语言跨平台兼容
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            return catalogJsonFromDB;
        }
        //转换为指定的对象
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return result;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        //锁的粒度越细越快
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> catalogJsonFromDB;
        try {
            catalogJsonFromDB = getCatalogJsonFromDB();
        } finally {
            lock.unlock();
        }
        return catalogJsonFromDB;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        //分布式锁
        String uuid = UUID.randomUUID().toString();
        //加锁必须原子操作
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 2000, TimeUnit.MILLISECONDS);
        if (lock) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDB;
            try {
                catalogJsonFromDB = getCatalogJsonFromDB();
            } finally {
                /*String lockValue = redisTemplate.opsForValue().get("lock");
            if (uuid.equals(lockValue)){
                redisTemplate.delete("lock");
            }*/
                //删锁必须原子操作 lua脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock", uuid));
            }
            return catalogJsonFromDB;
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            return getCatalogJsonFromDBWithRedisLock();//自旋
        }
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        synchronized (this) {
            return getCatalogJsonFromDB();
        }
    }

    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //根据当前二级分类找三级分类
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(collect);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找子菜单
     * Created by Howie on 2022/1/20.
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }
}