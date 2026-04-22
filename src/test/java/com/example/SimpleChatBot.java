package com.example;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.Delta;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
public class SimpleChatBot {

    // 聊天历史记录
    private static final List<ChatMessage> messageHistory = new ArrayList<>();
    private static ZhipuAiClient client;

    public static void main(String[] args) {
        initialize();
        startChat();
    }

    // 初始化客户端
    private static void initialize() {
        String apiKey = System.getenv("ZAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("错误: 未设置ZAI_API_KEY环境变量");
            System.err.println("请设置环境变量: export ZAI_API_KEY=your_api_key");
            System.exit(1);
        }

        client = ZhipuAiClient.builder().ofZHIPU()
                .apiKey(apiKey)
                .build();

        // 初始化系统消息
        messageHistory.add(ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM.value())
                .content("你是一个AI助手，请用简洁、友好的方式回答用户问题。")
                .build());
    }

    // 启动聊天
    private static void startChat() {
        Scanner scanner = new Scanner(System.in);

        printWelcome();

        while (true) {
            System.out.print("\n\033[32m你: \033[0m");
            String userInput = scanner.nextLine().trim();

            // 退出命令
            if (isExitCommand(userInput)) {
                System.out.println("再见！");
                break;
            }

            // 清屏命令
            if ("/clear".equalsIgnoreCase(userInput)) {
                clearHistory();
                System.out.println("对话历史已清空");
                continue;
            }

            // 查看历史命令
            if ("/history".equalsIgnoreCase(userInput)) {
                showHistory();
                continue;
            }

            // 帮助命令
            if ("/help".equalsIgnoreCase(userInput)) {
                showHelp();
                continue;
            }

            // 处理空输入
            if (userInput.isEmpty()) {
                continue;
            }

            // 添加到历史记录
            messageHistory.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(userInput)
                    .build());

            // 调用AI并获取回复
            String aiResponse = getAIResponse(userInput);

            // 将AI回复添加到历史记录
            messageHistory.add(ChatMessage.builder()
                    .role(ChatMessageRole.ASSISTANT.value())
                    .content(aiResponse)
                    .build());
        }

        scanner.close();
    }

    // 获取AI回复
    private static String getAIResponse(String userInput) {
        System.out.print("\n\033[34mAI: \033[0m");

        // 使用CountDownLatch等待流式响应完成
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder fullResponse = new StringBuilder();

        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.7-flash")  // 使用flash模型节省费用
                .messages(messageHistory)
                .maxTokens(1024)
                .temperature(0.7f)  // 降低随机性，使回复更稳定
                .topP(0.9f)
                .stream(true)
                .build();

        ChatCompletionResponse response = client.chat().createChatCompletion(request);

        if (response.isSuccess()) {
            response.getFlowable().subscribe(
                    data -> {
                        if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                            Delta delta = data.getChoices().get(0).getDelta();
                            if (delta.getContent() != null) {
                                System.out.print(delta.getContent());
                                System.out.flush();
                                fullResponse.append(delta.getContent());
                            }
                        }
                    },
                    error -> {
                        System.err.println("\n错误: " + error.getMessage());
                        latch.countDown();
                    },
                    () -> {
                        System.out.println();  // 换行
                        latch.countDown();
                    }
            );

            try {
                // 等待流式响应完成，最多等待30秒
                if (!latch.await(60, TimeUnit.SECONDS)) {
                    System.out.println("\n(响应超时)");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("\n(响应被中断)");
            }
        } else {
            System.err.println("API调用失败: " + response.getMsg());
            return "抱歉，我遇到了一些问题，暂时无法回答。";
        }

        return fullResponse.toString();
    }

    // 退出命令检测
    private static boolean isExitCommand(String input) {
        return input.equalsIgnoreCase("exit") ||
                input.equalsIgnoreCase("quit") ||
                input.equalsIgnoreCase("/exit") ||
                input.equalsIgnoreCase("/quit") ||
                input.equalsIgnoreCase("bye");
    }

    // 清空历史记录
    private static void clearHistory() {
        // 保留系统消息
        ChatMessage systemMessage = messageHistory.get(0);
        messageHistory.clear();
        messageHistory.add(systemMessage);
    }

    // 显示历史记录
    private static void showHistory() {
        System.out.println("\n=== 对话历史 ===");
        for (int i = 1; i < messageHistory.size(); i++) {  // 跳过系统消息
            ChatMessage msg = messageHistory.get(i);
            String role = msg.getRole();
            String content = (String) msg.getContent();

            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }

            String prefix = role.equals(ChatMessageRole.USER.value()) ? "你" : "AI";
            System.out.printf("%2d. [%s] %s\n", i, prefix, content);
        }
    }

    // 显示帮助
    private static void showHelp() {
        System.out.println("\n=== 帮助 ===");
        System.out.println("普通对话: 直接输入问题");
        System.out.println("退出: exit, quit, bye");
        System.out.println("清空历史: /clear");
        System.out.println("查看历史: /history");
        System.out.println("帮助: /help");
    }

    // 打印欢迎信息
    private static void printWelcome() {
        System.out.println("\n" +
                "╔══════════════════════════════════════╗\n" +
                "║      欢迎使用AI聊天机器人           ║\n" +
                "║      模型: GLM-4.7-Flash              ║\n" +
                "║      输入 /help 查看帮助            ║\n" +
                "║      输入 exit 退出                 ║\n" +
                "╚══════════════════════════════════════╝");
    }

    // 添加对话持久化功能（可选扩展）
    private static void saveConversation() {
        // 可以扩展为将对话保存到文件
    }

    private static void loadConversation() {
        // 可以扩展为从文件加载历史对话
    }
}