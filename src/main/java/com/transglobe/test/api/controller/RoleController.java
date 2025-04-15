package com.transglobe.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.transglobe.test.api.service.RoleService;
import com.transglobe.test.core.entity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping(value = "/api/role")
public class RoleController {

    private static final Logger log = LogManager.getLogger(RoleController.class);

    @Autowired
    RoleService roleService;
   

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public List<Role> read(HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException {

        String searchText = request.getParameter("searchText");
        log.info("讀取角色資料，搜尋條件: {}", searchText);

        List<Role> datas = new ArrayList<Role>();

        if (searchText == null || searchText.equals("")) {
            datas = roleService.findAll();
        } else {
            datas = roleService.findBySearchText(searchText);
        }

        return datas;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> create(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Role cmd) throws IOException, InterruptedException {
        
        log.info("新增角色: {}", cmd.getName());

        // 檢查角色ID是否已存在
        Optional<Role> existingRole = roleService.findById(cmd.getRoleId());
        if (existingRole.isPresent()) {
            log.warn("角色ID已存在: {}", cmd.getRoleId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("角色ID已存在");
        }

        // 設置建立者資訊
        String username = getUsernameFromRequest(request);
        cmd.setCrtUser(username);
        cmd.setUptUser(username);
        
        // 設置角色啟用狀態（如果未設置）
        if (cmd.getActive() == null) {
            cmd.setActive(true);
        }

        // 保存角色
        Role savedRole = roleService.save(cmd);
        log.info("角色新增成功: {}", savedRole.getRoleId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ResponseEntity<Object> update(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Role cmd) throws IOException, InterruptedException {
        
        log.info("更新角色: {}", cmd.getRoleId());

        // 檢查角色ID是否存在
        if (cmd.getRoleId() == null || cmd.getRoleId().trim().isEmpty()) {
            log.warn("角色ID不能為空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("角色ID不能為空");
        }

        // 設置更新者資訊
        String username = getUsernameFromRequest(request);
        cmd.setUptUser(username);

        // 更新角色
        Optional<Role> updatedRole = roleService.update(cmd);

        if (updatedRole.isEmpty()) {
            log.warn("找不到要更新的角色: {}", cmd.getRoleId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要更新的角色");
        }

        log.info("角色更新成功: {}", cmd.getRoleId());
        return ResponseEntity.ok(updatedRole.get());
    }

    @RequestMapping(value = "/delete/{roleId}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("roleId") String roleId) throws IOException, InterruptedException {
        
        log.info("刪除Role: {}", roleId);
        
        Boolean result = roleService.delete(roleId);
        
        if (result == null || !result) {
            log.warn("找不到要刪除的role: {}", roleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要刪除的role");
        }
        
        log.info("role刪除成功: {}", roleId);
        return ResponseEntity.ok("role刪除成功");
    }
    
    // 輔助方法：從請求中獲取用戶名
    private String getUsernameFromRequest(HttpServletRequest request) {
        // 實際應用中，可能從JWT或Session中獲取
        // 這裡只是一個簡單的示例
        String username = request.getHeader("X-User-Name");
        return username != null ? username : "system";
    }
}