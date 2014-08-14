package com.iisquare.jees.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iisquare.jees.demo.dao.TestDao;
import com.iisquare.jees.framework.model.ServiceBase;

@Service
public class TestService extends ServiceBase {
	
	@Autowired
	public TestDao testDao;
	
	public TestService() {}
	
	public int insert(Map<String, Object> values) {
		return testDao.insert(values);
	}
	
	public Map<String, Object> getById(int id) {
		return testDao.getById(id);
	}
}
