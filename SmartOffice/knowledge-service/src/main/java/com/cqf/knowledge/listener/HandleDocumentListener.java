package com.cqf.knowledge.listener;

import com.cqf.common.constants.MQConstants;
import com.cqf.common.domain.dto.DocumentChunkMsg;
import com.cqf.common.service.NoticeWebSocketService;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.service.IKbDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleDocumentListener {
    private final RabbitTemplate rabbitTemplate;
    private final IKbDocumentService kbDocumentService;
    private final Tika tika;

    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.DOCUMENT_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = MQConstants.EXCHANGE_NAME),
            key = MQConstants.DOCUMENT_KEY
    ))
    public void listenDocumentTask(HashMap<String, Object> map) throws TikaException {
        Object id = map.get("documentId");
        Object username = map.get("username");
        KbDocument document = kbDocumentService.lambdaQuery().eq(KbDocument::getId, id).one();
        if (document == null) {
            return;
        }
        if (document.getStatus() != 0)return;
        InputStream inputStream = null;
        try {
            document.setStatus(1);
            kbDocumentService.updateById(document);

            String fileType = document.getFileType();
            String fileUrl = document.getFileUrl();
            String objectName = fileUrl.substring(13);
            inputStream = kbDocumentService.downloadFileFromMinIO(bucketFiles,objectName );

            String content = parseContent(inputStream, fileType);
            document.setContent(content);
            document.setStatus(1);//状态 0待处理 1处理中 2已完成 3处理失败
            kbDocumentService.updateById(document);
            // 发送MQ消息给ai-service进行向量化
            DocumentChunkMsg msg = new DocumentChunkMsg();
            msg.setKbId(document.getKbId());
            msg.setDocumentId(document.getId());
            msg.setFileUrl(fileUrl);
            msg.setFileName(document.getTitle());
            msg.setUsername(username.toString());
            rabbitTemplate.convertAndSend(MQConstants.EXCHANGE_NAME_AI,MQConstants.DOCUMENT_KEY_AI,msg);
        } catch (IOException e) {
            log.error("解析文档内容失败，文档ID：{}", id, e);
            document.setStatus(3);
            kbDocumentService.updateById(document);
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }


    }

    /**
     * 根据文件类型解析文档内容
     * <p>
     * 支持的格式：
     * <ul>
     *   <li>PDF (.pdf) - 使用Tika自动解析</li>
     *   <li>Word (.doc, .docx) - 使用Tika自动解析</li>
     *   <li>Text (.txt) - 使用Tika自动解析</li>
     *   <li>HTML (.html, .htm) - 使用Tika自动解析</li>
     *   <li>Markdown (.md) - 使用Tika自动解析</li>
     *   <li>其他格式 - 使用Tika自动解析</li>
     * </ul>
     * TODO:对解析后的内容进行分块
     * @param fileType  文件类型（MIME类型或文件扩展名）
     * @return 解析后的文本内容
     * @throws IOException   IO异常
     * @throws SAXException  XML解析异常
     * @throws TikaException Tika解析异常
     */
    private String parseContent(InputStream stream, String fileType) throws IOException, TikaException {
//        InputStream stream = new ByteArrayInputStream(fileBytes);
        // Tika会自动识别文件类型并选择合适的解析器
        // 支持PDF、Word、Text、HTML等
        /*// 根据文件类型选择解析器
        if (fileType != null && (fileType.toLowerCase().endsWith("pdf") || fileType.equals("application/pdf"))) {
            // PDF文件使用专门的PDF解析器
            PDFParser parser = new PDFParser();
            parser.parse(stream, handler, metadata, context);
        } else {
            // 其他格式使用Tika自动识别并解析
            // Tika能自动识别文件类型并选择合适的解析器
            tika.parse(stream, handler);
        }
*/
        return tika.parseToString(stream);
    }
}
