package com.transglobe.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.transglobe.test.api.service.MenuService;
import com.transglobe.test.core.entity.Menu;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping(value = "/api/menu")
public class MenuController {

    private static final Logger log = LogManager.getLogger(MenuController.class);

    @Autowired
    MenuService menuService;

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public List<Menu> read(HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException {

        String searchText = request.getParameter("searchText");
        log.info("讀取選單資料，搜尋條件: {}", searchText);

        List<Menu> datas = new ArrayList<Menu>();

        if (searchText == null || searchText.equals("")) {
            datas = menuService.findAll();
        } else {
            datas = menuService.findBySearchText(searchText);
        }

        return datas;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> create(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Menu cmd) throws IOException, InterruptedException {
        
        log.info("新增選單: {}", cmd.getName());

        // 檢查選單ID是否已存在
        Optional<Menu> existingMenu = menuService.findById(cmd.getMenuId());
        if (existingMenu.isPresent()) {
            log.warn("選單ID已存在: {}", cmd.getMenuId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("選單ID已存在");
        }

        // 保存選單
        Menu savedMenu = menuService.save(cmd);
        log.info("選單新增成功: {}", savedMenu.getMenuId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMenu);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ResponseEntity<Object> update(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Menu cmd) throws IOException, InterruptedException {
        
        log.info("更新選單: {}", cmd.getMenuId());

        // 更新選單
        Optional<Menu> updatedMenu = menuService.update(cmd);

        if (updatedMenu.isEmpty()) {
            log.warn("找不到要更新的選單: {}", cmd.getMenuId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要更新的選單");
        }

        log.info("選單更新成功: {}", cmd.getMenuId());
        return ResponseEntity.ok(updatedMenu.get());
    }

    @RequestMapping(value = "/delete/{menuId}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("menuId") String menuId) throws IOException, InterruptedException {
        
        log.info("刪除選單: {}", menuId);
        
        Boolean result = menuService.delete(menuId);
        
        if (result == null || !result) {
            log.warn("找不到要刪除的選單: {}", menuId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到要刪除的選單");
        }
        
        log.info("選單刪除成功: {}", menuId);
        return ResponseEntity.ok("選單刪除成功");
    }
}