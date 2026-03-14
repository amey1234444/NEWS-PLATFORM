package org.example.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatRepository {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveChat(String userId, String userMsg, String aiMsg) {

        String key = "chat:" + userId;

        redisTemplate.opsForList().rightPush(key, "User: " + userMsg);
        redisTemplate.opsForList().rightPush(key, "AI: " + aiMsg);
    }

    public String getChatHistory(String userId) {

        String key = "chat:" + userId;

        List<String> history = redisTemplate.opsForList().range(key, 0, -1);

        if(history == null) return "";

        return String.join("\n", history);
    }
}
