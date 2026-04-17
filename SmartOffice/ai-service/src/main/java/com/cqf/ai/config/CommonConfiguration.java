package com.cqf.ai.config;



import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory.builder().build();
    }

//    @Bean
//    public RestClient restClient() {
//        return RestClient.builder(new HttpHost("192.168.220.100", 9200, "http"))
//                .build();
//    }
//    @Bean
//    public VectorStore vectorStoretest(RestClient restClient, EmbeddingModel embeddingModel) {
//        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
//        options.setIndexName("kb_document_chunk");    // Optional: defaults to "spring-ai-document-index"
//
//        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
//                .build();
//    }



    /*@Bean
    public ChatClient pdfChatClient(OpenAiChatModel model, ChatMemory chatMemory,VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem("请根据上下文回答问题,遇到上下文没有的问题,不要随意编造")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),//添加日志
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),//添加会话存储记录
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().topK(2).similarityThreshold(0.6).build())
                                .build()

                )
                .build();
    }*/

}
