package com.java.service.Impl;

import com.java.mapper.UsersMapper;
import com.java.pojo.Users;
import com.java.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@CacheConfig(cacheNames="users")
public class UsersServiceImpl implements UsersService {
    @Autowired
    UsersMapper usersMapper;
    private static final String SALT = "randomString";
    private static final int ALLOW_COUNT = 10;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Override
    @CacheEvict(allEntries=true)
    public void add(Users c) {
        usersMapper.save(c);
    }

    @Override
    @CacheEvict(key="'users-one-'+ #p0")
    public void delete(int id) {
        usersMapper.delete(id);
    }

    @Override
    @CacheEvict(allEntries=true)
    public void update(Users c) {
        usersMapper.update(c);
    }

    @Override
    @Cacheable(key="'users-one-'+ #p0", unless="#result == null")
    public Users get(int id) {
        return usersMapper.get(id);
    }

    @Override
//    @Cacheable(key="'users-all'")
    public List<Users> list() {
        return usersMapper.findAll();
    }

    @Override
    public Boolean isExist(String name) {
        Users user = getByName(name);
        return null!=user;
    }
    @Override
    @Cacheable(key="'users-one-name-'+ #p0", unless="#result == null")
    public Users getByName(String name) {
        return usersMapper.findByName(name);
    }

    @Override
    @Cacheable(key="'users-one-name-'+ #p0 +'-password-'+ #p1", unless="#result == null")
    public Users getByNameAndPassword(String name, String password){
        return usersMapper.findByNameAndPassword(name, password);
    }

    @Override
    public int addUserCount(Integer userId) {
        String limitKey = "seckill_user_limit_" + userId;//缓存名
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);//用户访问频率
        int limit = -1;
        if (limitNum == null) {
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = "seckill_user_limit_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            LOGGER.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > ALLOW_COUNT;
    }
}
