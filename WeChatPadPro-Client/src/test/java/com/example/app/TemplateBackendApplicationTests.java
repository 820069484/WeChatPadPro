package com.example.app;

import com.alibaba.fastjson2.JSON;
import com.example.app.entity.dto.PageRequest;
import io.jsonwebtoken.*;
import java.util.Date;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TemplateBackendApplicationTests {

    @Resource
    private RedisUtils redisUtils;

    @Test
    void contextLoads() {
        JwtBuilder jwtBuilder = Jwts.builder();
        String sign = "secret";
        String token = jwtBuilder.setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("username", "tom")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS256, sign)
                .compact();
        System.out.println(token);
    }

    @Test
    void parseToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRvbSIsImV4cCI6MTc0MzU5MTE0MX0.dTBUL4LNa4jMChkhkcDzzL6f9Ufk2ZP_z1lQLu6p7FA";
        JwtParser jwtParser = Jwts.parser();
        Jws<Claims> claimsJws = jwtParser.setSigningKey("secret").parseClaimsJws(token);
        Claims body = claimsJws.getBody();
        System.out.println(body.get("username"));
    }

    @Test
    void testRedis() {
        redisUtils.set("test123", "123", -1L);
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(0);
        pageRequest.setPageSize(0);
        pageRequest.setSortField("");
        pageRequest.setSortOrder("");
        redisUtils.delete("test123");
        String jsonStr = JSON.toJSONString(pageRequest);
        redisUtils.set("test1234", jsonStr, 20);
        System.out.println(redisUtils.get("test123"));
    }

}
