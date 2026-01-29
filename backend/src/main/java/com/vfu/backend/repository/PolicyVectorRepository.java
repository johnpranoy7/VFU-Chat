package com.vfu.backend.repository;

import com.vfu.backend.model.PolicyVector;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Repository
public class PolicyVectorRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public PolicyVectorRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(List<PolicyVector> vectors) {
        try {
            String json = mapper.writeValueAsString(vectors);
            redisTemplate.opsForValue().set("policies:all", json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PolicyVector> findAll() {
        String json = redisTemplate.opsForValue().get("policies:all");
        if (json == null) return List.of();

        try {
            return Arrays.asList(mapper.readValue(json, PolicyVector[].class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
