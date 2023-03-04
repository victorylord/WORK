package com.panda.sport.rcs.trade.init;

import org.springframework.stereotype.Component;

import com.panda.sport.rcs.trade.wrapper.impl.RcsOddsConvertMappingServiceImpl;

@Component
public class OddsConverMapping {
	
	public OddsConverMapping(RcsOddsConvertMappingServiceImpl rcsOddsConvertMappingService) {
    	rcsOddsConvertMappingService.init();
    }

}
