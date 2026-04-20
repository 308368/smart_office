package com.cqf.ai.listener;

import com.cqf.ai.service.IDocumentVectorService;
import com.cqf.common.domain.dto.DocumentChunkMsg;
import com.cqf.common.constants.MQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentVectorConsumer {
    private final IDocumentVectorService documentVectorService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.DOCUMENT_QUEUE_NAME_AI, durable = "true"),
            exchange = @Exchange(value = MQConstants.EXCHANGE_NAME_AI),
            key = MQConstants.DOCUMENT_KEY_AI
    ))
    public void handleDocumentMessage(DocumentChunkMsg msg){
        log.info("收到文档消息: documentId={}", msg.getDocumentId());
        try {
            documentVectorService.processDocument(msg);
        } catch (Exception e) {
            log.error("文档处理失败: documentId={}", msg.getDocumentId(), e);
        }
    }
}
