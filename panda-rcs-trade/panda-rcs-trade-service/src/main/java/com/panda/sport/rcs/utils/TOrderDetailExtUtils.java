package com.panda.sport.rcs.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@RefreshScope
public class TOrderDetailExtUtils {

    @Value("${rcs.tOrderDetailExt.saveToMongo:false}")
    private boolean saveToMongo;

}
