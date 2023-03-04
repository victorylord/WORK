package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import lombok.Data;

import java.util.List;

@Data
public class TournamentTemplateAddEventParam extends RcsTournamentTemplate {
    /**
     * 玩法赔率源
     */
    private List<EventTypeConfig> eventList;

    @Data
    public static class EventTypeConfig {
        /**
         * 事件类型
         */
        private String eventType;
        /**
         * 事件
         */
        private List<EventConfig> eventConfig;
    }

    @Data
    public static class EventConfig {
        /**
         * 事件编码
         */
        private String eventCode;
        /**
         * 事件名称
         */
        private String eventName;
        /**
         * 是否点球  0：否    1：是
         */
        private Integer isPenalty;
    }
}
