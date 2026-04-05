package com.cqf.common.utils;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

@RequiredArgsConstructor
@Slf4j
public class RabbitMqHelper {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object msg){
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);

    }

    public void sendDelayMessage(String exchange, String routingKey, Object msg, int delay){
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,message -> {
            message.getMessageProperties().setDelay(delay);
            return message;
        });

    }

    public void sendMessageWithConfirm(String exchange, String routingKey, Object msg, int maxRetries){
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString(true));
        cd.getFuture().whenComplete((result, ex) -> {
            int retryCount = 0;
            if (ex != null) {
                log.error("处理 ack 回执失败", ex);
            } else if (result != null && !result.isAck()) {
                if (retryCount >= maxRetries) {
                    log.error("次数耗尽");
                    return;
                }
                CorrelationData newCd = new CorrelationData(UUID.randomUUID().toString(true));
                int finalRetryCount = retryCount;
                newCd.getFuture().whenComplete((r, e) -> {
                    // 递归回调处理
                    handleConfirmResult(r, e, exchange, routingKey, msg, maxRetries, finalRetryCount + 1);
                });
                rabbitTemplate.convertAndSend(exchange, routingKey, msg, newCd);
                retryCount++;
            }
        });
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
    }

    private void handleConfirmResult(CorrelationData.Confirm result, Throwable ex,
                                     String exchange, String routingKey, Object msg,
                                     int maxRetries, int retryCount) {
        if (ex != null) {
            log.error("处理 ack 回执失败", ex);
            return;
        }

        if (result != null && !result.isAck()) {
            if (retryCount >= maxRetries) {
                log.error("次数耗尽");
                return;
            }
            CorrelationData newCd = new CorrelationData(UUID.randomUUID().toString(true));
            newCd.getFuture().whenComplete((r, e) -> {
                handleConfirmResult(r, e, exchange, routingKey, msg, maxRetries, retryCount + 1);
            });
            rabbitTemplate.convertAndSend(exchange, routingKey, msg, newCd);
        }
    }
}