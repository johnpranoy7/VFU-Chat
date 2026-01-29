package com.vfu.backend.repository;

import com.vfu.backend.model.PolicyVector;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class PolicyVectorRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public PolicyVectorRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(PolicyVector vector) {
        redisTemplate.opsForValue()
                .set("policy:" + vector.getId(), vector);
    }

    public List<PolicyVector> findAll() {
        Set<String> keys = redisTemplate.keys("policy:*");
        if (keys == null) return List.of();

        return keys.stream()
                .map(k -> (PolicyVector) redisTemplate.opsForValue().get(k))
                .toList();
    }
}
