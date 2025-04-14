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

import com.transglobe.test.core.entity.Menu;
import com.transglobe.test.core.repository.MenuRepository;

@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger log = LogManager.getLogger(MenuServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    MenuRepository menuRepository;

    @Override
    public List<Menu> findAll() {
        return menuRepository.findAll();
    }

    @Override
    public List<Menu> findBySearchText(String searchText) {
        // TODO: 實現搜尋邏輯
        return null;
    }

    @Override
    public Optional<Menu> findById(String menuId) {
        return menuRepository.findById(menuId);
    }

    @Override
    public Menu save(Menu cmd) {
        // 取得目前時間
        long nowMillis = Instant.now().toEpochMilli();
        LocalDateTime dateTime = Instant.ofEpochMilli(nowMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        String formattedDateTime = dateTime.format(formatter);
        
        // 設置所有來自傳入對象的屬性
        cmd.setCrtTime(nowMillis);
        cmd.setCrtTimeTxt(formattedDateTime);
        cmd.setUptTime(nowMillis);
        cmd.setUptTimeTxt(formattedDateTime);
        
        return menuRepository.save(cmd);
    }

    @Override
    public Optional<Menu> update(Menu cmd) {
        if (!menuRepository.existsById(cmd.getMenuId())) {
            return Optional.empty();
        }

        // 取得原始菜單
        Menu existingMenu = menuRepository.findById(cmd.getMenuId()).get();
        
        // 保留建立時間資訊
        cmd.setCrtTime(existingMenu.getCrtTime());
        cmd.setCrtTimeTxt(existingMenu.getCrtTimeTxt());
        
        // 設置更新時間
        long nowMillis = Instant.now().toEpochMilli();
        LocalDateTime dateTime = Instant.ofEpochMilli(nowMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        
        cmd.setUptTime(nowMillis);
        cmd.setUptTimeTxt(dateTime.format(formatter));
        
        // 保存並返回更新後的菜單
        Menu updatedMenu = menuRepository.save(cmd);
        return Optional.of(updatedMenu);
    }

    @Override
    public Boolean delete(Menu cmd) {
    	
        if (!menuRepository.existsById(cmd.getMenuId())) {
            return false;
        }
        
        menuRepository.deleteById(cmd.getMenuId());
        return true;
    }
}