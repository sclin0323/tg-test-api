package com.transglobe.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.transglobe.test.api.service.UserService;
import com.transglobe.test.core.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public List<User> read(HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException {

        String searchText = request.getParameter("searchText");
        log.info("讀取使用者資料，搜尋條件: {}", searchText);

        List<User> datas = new ArrayList<User>();

        if (searchText == null || searchText.equals("")) {
            datas = userService.findAll();
        } else {
            datas = userService.findBySearchText(searchText);
        }

        return datas;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> create(HttpServletRequest request, HttpServletResponse response,
            @RequestBody User cmd) throws IOException, InterruptedException {
        
        log.info("新增使用者: {}", cmd.getName());

        // 檢查使用者ID是否已存在
        Optional<User> existingUser = userService.findById(cmd.getUserId());
        if (existingUser.isPresent()) {
            log.warn("使用者ID已存在: {}", cmd.getUserId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("使用者ID已存在");
        }

        // 設置建立者資訊
        String username = getUsernameFromRequest(request);
        cmd.setCrtUser(username);
        cmd.setUptUser(username);
        
        // 設置使用者啟用狀態（如果未設置）
        if (cmd.getActive() == null) {
            cmd.setActive(true);
        }

        // 保存使用者
        User savedUser = userService.save(cmd);
        log.info("使用者新增成功: {}", savedUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ResponseEntity<Object> update(HttpServletRequest request, HttpServletResponse response,
            @RequestBody User cmd) throws IOException, InterruptedException {
        
        log.info("更新使用者: {}", cmd.getUserId());

        // 檢查使用者ID是否存在
        if (cmd.getUserId() == null || cmd.getUserId().trim().isEmpty()) {
            log.warn("使用者ID不能為空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("使用者ID不能為空");
        }

        // 設置更新者資訊
        String username = getUsernameFromRequest(request);
        cmd.setUptUser(username);

        // 更新使用者
        Optional<User> updatedUser = userService.update(cmd);

        if (updatedUser.isEmpty()) {
            log.warn("找不到要更新的使用者: {}", cmd.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要更新的使用者");
        }

        log.info("使用者更新成功: {}", cmd.getUserId());
        return ResponseEntity.ok(updatedUser.get());
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(HttpServletRequest request, HttpServletResponse response,
            @RequestBody User cmd) throws IOException, InterruptedException {
        
        log.info("刪除使用者: {}", cmd.getUserId());
        
        Boolean result = userService.delete(cmd);
        
        if (result == null || !result) {
            log.warn("找不到要刪除的使用者: {}", cmd.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要刪除的使用者");
        }
        
        log.info("使用者刪除成功: {}", cmd.getUserId());
        return ResponseEntity.ok("使用者刪除成功");
    }
    
    // 輔助方法：從請求中獲取用戶名
    private String getUsernameFromRequest(HttpServletRequest request) {
        // 實際應用中，可能從JWT或Session中獲取
        // 這裡只是一個簡單的示例
        String username = request.getHeader("X-User-Name");
        return username != null ? username : "system";
    }
}