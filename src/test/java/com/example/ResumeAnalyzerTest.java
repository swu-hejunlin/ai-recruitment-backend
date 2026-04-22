package com.example;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

public class ResumeAnalyzerTest {

    public static void main(String[] args) {

        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                .apiKey(System.getenv("ZAI_API_KEY"))
                .build();
        // ==========================================
        // 1. 准备本地简历路径
        // ==========================================
        String filePath = "D:\\工作学习\\1秋招找工作\\何俊林求职简历.pdf"; // 替换为你的真实路径

        try {
            // ==========================================
            // 2. 读取文件并转 Base64 (复用之前的工具类)
            // ==========================================
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String fileName = filePath.toLowerCase();
            System.out.println("输出PDF文字");
            System.out.println(PdfUtil.extractTextFromPdf(fileBytes));
            System.out.println("输出PDF清理后的文字");
            System.out.println(PdfUtil.extractAndCleanText(fileBytes));

            String base64Str;
            String mimeType;

            if (fileName.endsWith(".pdf")) {
                System.out.println("检测到PDF，转换为图片...");
                base64Str = PdfUtil.convertPdfFirstPageToBase64(fileBytes);
                mimeType = "image/png";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                base64Str = Base64.getEncoder().encodeToString(fileBytes);
                mimeType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                base64Str = Base64.getEncoder().encodeToString(fileBytes);
                mimeType = "image/png";
            } else {
                System.err.println("不支持的格式");
                return;
            }

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model("glm-4.6v-flash")
                    .messages(Arrays.asList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(Arrays.asList(
                                            MessageContent.builder()
                                                    .type("text")
                                                    .text("你是一个资深HR，从夯到拉，锐评这份简历，有五个等级从好到差，夯，顶级，人上人，npc，拉完了，直接给出你的评级")
                                                    .build(),
                                            MessageContent.builder()
                                                    .type("image_url")
                                                    .imageUrl(ImageUrl.builder()
                                                            .url(base64Str)
                                                            .build())
                                                    .build()))
                                    .build()
                    ))
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (response.isSuccess()) {
                System.out.println(response.getData().getChoices().get(0).getMessage().getContent());
            }else {
                System.err.println("错误: " + response.getMsg());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}