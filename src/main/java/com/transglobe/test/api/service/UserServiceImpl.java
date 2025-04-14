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

import com.transglobe.test.core.entity.User;
import com.transglobe.test.core.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LogManager.getLogger(UserServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findBySearchText(String searchText) {
        // 實現搜尋邏輯 - 您可能需要在 UserRepository 中添加一個方法
        // 例如: return userRepository.findByNameContainingIgnoreCase(searchText);
        return null;
    }

    @Override
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public User save(User cmd) {
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
        
        return userRepository.save(cmd);
    }

    @Override
    @Transactional
    public Optional<User> update(User cmd) {
        if (!userRepository.existsById(cmd.getUserId())) {
            return Optional.empty();
        }

        // 取得原始使用者
        User existingUser = userRepository.findById(cmd.getUserId()).get();
        
        // 保留建立時間和建立者資訊
        cmd.setCrtTime(existingUser.getCrtTime());
        cmd.setCrtTimeTxt(existingUser.getCrtTimeTxt());
        cmd.setCrtUser(existingUser.getCrtUser());
        
        // 如果沒有設置，也保留原有的角色關聯
        if (cmd.getRoles() == null && existingUser.getRoles() != null) {
            cmd.setRoles(existingUser.getRoles());
        }
        
        // 設置更新時間
        long nowMillis = Instant.now().toEpochMilli();
        LocalDateTime dateTime = Instant.ofEpochMilli(nowMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        String formattedDateTime = dateTime.format(formatter);
        
        cmd.setUptTime(nowMillis);
        cmd.setUptTimeTxt(formattedDateTime);
        
        // 保存並返回更新後的使用者
        User updatedUser = userRepository.save(cmd);
        return Optional.of(updatedUser);
    }

    @Override
    @Transactional
    public Boolean delete(User cmd) {
        if (!userRepository.existsById(cmd.getUserId())) {
            return false;
        }
        
        userRepository.deleteById(cmd.getUserId());
        return true;
    }
}