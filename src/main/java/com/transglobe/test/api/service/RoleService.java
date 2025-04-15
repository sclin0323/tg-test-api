package com.transglobe.test.api.service;

import java.util.List;
import java.util.Optional;

import com.transglobe.test.core.entity.Role;

public interface RoleService {
	
	public List<Role> findAll();
	
	public List<Role> findBySearchText(String searchText);
	
	public Optional<Role> findById(String menuId);
	
	public Role save(Role o);
	
	public Optional<Role> update(Role o);

	public Boolean delete(String id);

}
