package org.evolve.auth.api;

import cn.hutool.crypto.digest.BCrypt;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于密码测试，生产环境应删除
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/password")
    public Result<Map<String, Object>> testPassword(@RequestParam String password) {
        // 生成哈希
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        // 验证哈希
        boolean check = BCrypt.checkpw(password, hashed);

        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hashed", hashed);
        result.put("verified", check);
        result.put("algorithm", "$2a$10$");

        return Result.ok(result);
    }

    @PostMapping("/verify")
    public Result<Map<String, Object>> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean check = BCrypt.checkpw(password, hash);

        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("verified", check);

        return Result.ok(result);
    }
}
