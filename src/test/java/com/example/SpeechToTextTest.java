package com.example;

import com.example.airecruitmentbackend.util.SpeechToTextUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

/**
 * 语音转文字测试类
 */
public class SpeechToTextTest {

    /**
     * 测试同步语音识别
     */
    @Test
    public void testRecognizeSync() {
        try {
            // 这里使用测试音频文件路径，实际使用时需要替换为真实的音频文件路径
            String audioFilePath = "C:\\Users\\何俊林\\Music\\标准录音 11.mp3";
            System.out.println("开始测试同步语音识别...");
            String result = SpeechToTextUtil.recognizeSync(audioFilePath);
            System.out.println("识别结果:");
            System.out.println(result);
            System.out.println("同步语音识别测试完成");
        } catch (Exception e) {
            System.err.println("同步语音识别测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试异步语音识别
     */
    @Test
    public void testRecognizeAsync() {
        try {
            // 这里使用测试音频文件路径，实际使用时需要替换为真实的音频文件路径
            String audioFilePath = "C:\\Users\\何俊林\\Music\\标准录音 11.mp3";
            System.out.println("开始测试异步语音识别...");
            CompletableFuture<String> future = SpeechToTextUtil.recognizeAsync(audioFilePath);
            // 等待结果
            String result = future.get();
            System.out.println("识别结果:");
            System.out.println(result);
            System.out.println("异步语音识别测试完成");
        } catch (Exception e) {
            System.err.println("异步语音识别测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
