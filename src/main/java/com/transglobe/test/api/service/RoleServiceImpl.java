package com.transglobe.test.api.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transglobe.test.core.entity.Role;
import com.transglobe.test.core.repository.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LogManager.getLogger(RoleServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    RoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public List<Role> findBySearchText(String searchText) {
        // 實現搜尋邏輯 - 您可能需要在 RoleRepository 中添加一個方法
        // 例如: return roleRepository.findByNameContainingIgnoreCase(searchText);
        return null;
    }

    @Override
    public Optional<Role> findById(String roleId) {
        return roleRepository.findById(roleId);
    }

    @Override
    @Transactional
    public Role save(Role cmd) {
        // 取得目前時間
        long nowMillis = Instant.now().toEpochMilli();
        LocalDateTime dateTime = Instant.ofEpochMilli(nowMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        String formattedDateTime = dateTime.format(formatter);
        
        // 設置時間戳和格式化時間
        cmd.setCrtTime(nowMillis);
        cmd.setCrtTimeTxt(formattedDateTime);
        cmd.setUptTime(nowMillis);
        cmd.setUptTimeTxt(formattedDateTime);
        
        return roleRepository.save(cmd);
    }

    @Override
    @Transactional
    public Optional<Role> update(Role cmd) {
        if (!roleRepository.existsById(cmd.getRoleId())) {
            return Optional.empty();
        }

        // 取得原始角色
        Role existingRole = roleRepository.findById(cmd.getRoleId()).get();
        
        // 保留建立時間和建立者資訊
        cmd.setCrtTime(existingRole.getCrtTime());
        cmd.setCrtTimeTxt(existingRole.getCrtTimeTxt());
        cmd.setCrtUser(existingRole.getCrtUser());
        
        // 如果沒有設置，也保留原有的菜單關聯
        if (cmd.getMenus() == null && existingRole.getMenus() != null) {
            cmd.setMenus(existingRole.getMenus());
        }
        
        // 設置更新時間
        long nowMillis = Instant.now().toEpochMilli();
        LocalDateTime dateTime = Instant.ofEpochMilli(nowMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        String formattedDateTime = dateTime.format(formatter);
        
        cmd.setUptTime(nowMillis);
        cmd.setUptTimeTxt(formattedDateTime);
        
        // 保存並返回更新後的角色
        Role updatedRole = roleRepository.save(cmd);
        return Optional.of(updatedRole);
    }

    @Override
    @Transactional
    public Boolean delete(Role cmd) {
        if (!roleRepository.existsById(cmd.getRoleId())) {
            return false;
        }
        
        roleRepository.deleteById(cmd.getRoleId());
        return true;
    }
}