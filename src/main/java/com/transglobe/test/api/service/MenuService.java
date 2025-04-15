package com.transglobe.test.api.service;

import java.util.List;
import java.util.Optional;

import com.transglobe.test.core.entity.Menu;

public interface MenuService {
	
	public List<Menu> findAll();
	
	public List<Menu> findBySearchText(String searchText);
	
	public Optional<Menu> findById(String menuId);
	
	public Menu save(Menu o);
	
	public Optional<Menu> update(Menu o);

	public Boolean delete(String id);
}
