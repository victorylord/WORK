package com.panda.sport.rcs.mapper;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface RcsMonitorMapper {

	void cleanData(Map<String, String> consumer);

}
