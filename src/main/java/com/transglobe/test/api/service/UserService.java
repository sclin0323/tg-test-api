package com.transglobe.test.api.service;

import java.util.List;
import java.util.Optional;

import com.transglobe.test.core.entity.Menu;
import com.transglobe.test.core.entity.User;

public interface UserService {
	
	public List<User> findAll();
	
	public List<User> findBySearchText(String searchText);
	
	public Optional<User> findById(String userId);
	
	public User save(User o);
	
	public Optional<User> update(User o);

	public Boolean delete(User o);
}
