package com.transglobe.test.data.init;



import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.transglobe.test.core.entity.Menu;
import com.transglobe.test.core.entity.Role;
import com.transglobe.test.core.repository.MenuRepository;
import com.transglobe.test.core.repository.RoleRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

//@Profile("dev") // 只有 dev profile 會執行（可選）
@Component
public class DatabaseSeeder implements ApplicationListener<ContextRefreshedEvent> {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    private boolean alreadySetup = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;
        
        Long nowTime = new Date().getTime();
        
        // create menus samples
        Optional<Menu> m01 = menuRepository.findById("MENU01");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU01");
        	o.setName("主表單01");
        	o.setPath("menu01");
        	o.setLevel(1);
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        Optional<Menu> m02 = menuRepository.findById("MENU02");
        if (m02.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU02");
        	o.setName("主表單02");
        	o.setPath("menu02");
        	o.setLevel(1);
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        
        
        Optional<Menu> m01_1 = menuRepository.findById("MENU01_1");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU01_1");
        	o.setName("子表單01_1");
        	o.setPath("menu01_1");
        	o.setLevel(2);
        	o.setParentMenuId("MENU01");
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        Optional<Menu> m01_2 = menuRepository.findById("MENU01_2");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU01_2");
        	o.setName("子表單01_2");
        	o.setPath("menu01_2");
        	o.setLevel(2);
        	o.setParentMenuId("MENU01");
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        Optional<Menu> m01_3 = menuRepository.findById("MENU01_3");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU01_3");
        	o.setName("子表單01_3");
        	o.setPath("menu01_3");
        	o.setLevel(2);
        	o.setParentMenuId("MENU01");
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        
        Optional<Menu> m02_1 = menuRepository.findById("MENU02_1");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU02_1");
        	o.setName("子表單02_1");
        	o.setPath("menu02_1");
        	o.setLevel(2);
        	o.setParentMenuId("MENU02");
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        Optional<Menu> m02_2 = menuRepository.findById("MENU02_2");
        if (m01.isEmpty()) {
        	Menu o = new Menu();
        	o.setMenuId("MENU02_2");
        	o.setName("子表單02_2");
        	o.setPath("menu02_2");
        	o.setLevel(2);
        	o.setParentMenuId("MENU02");
        	
        	o.setCrtTime(nowTime);
        	o.setCrtTimeTxt(sdf.format(nowTime));
        	
        	o.setUptTime(nowTime);
        	o.setUptTimeTxt(sdf.format(nowTime));
        	
        	menuRepository.save(o);
        }
        
        
        

        // 建立預設角色
        Optional<Role> adminRole = roleRepository.findById("admin");
        if (adminRole.isEmpty()) {
            Role role = new Role();
            role.setRoleId("admin");
            role.setName("系統管理者");
            role.setActive(true);
            //roleRepository.save(role);
        }
        
        // 建立預設的user
        

        // 建立預設選單
        Optional<Menu> homeMenu = menuRepository.findById("menu-home");
        if (homeMenu.isEmpty()) {
            Menu menu = new Menu();
            menu.setMenuId("menu-home");
            menu.setName("首頁");
            menu.setPath("/home");
            menu.setLevel(1);
            //menuRepository.save(menu);
        }

        alreadySetup = true;
    }
}

