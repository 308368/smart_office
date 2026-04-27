package com.cqf.common.config;

import com.cqf.common.service.NoticeWebSocketService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
@ConditionalOnClass({SimpMessagingTemplate.class})
@ConditionalOnBean({SimpMessagingTemplate.class})
@AutoConfigureAfter(WebSocketConfig.class)
public class NoticeWebSocketConfig {

    @Bean
    @ConditionalOnMissingBean
    public NoticeWebSocketService noticeWebSocketService(SimpMessagingTemplate simpMessagingTemplate) {
        return new NoticeWebSocketService(simpMessagingTemplate);
    }
}
