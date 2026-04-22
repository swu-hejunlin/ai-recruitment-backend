package com.example;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import ai.z.openapi.core.Constants;
import java.util.Arrays;

public class BasicChat {
    public static void main(String[] args) {
        // 初始化客户端
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                .apiKey(System.getenv("ZAI_API_KEY"))
                .build();

        // 创建聊天完成请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.7-flash")
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content("你好，请介绍一下自己")
                                .build()
                ))
                .build();

        // 发送请求
        ChatCompletionResponse response = client.chat().createChatCompletion(request);

        // 获取回复
        if (response.isSuccess()) {
            Object reply = response.getData().getChoices().get(0).getMessage();
            System.out.println("AI 回复: " + reply);
        } else {
            System.err.println("错误: " + response.getMsg());
        }
    }
}
