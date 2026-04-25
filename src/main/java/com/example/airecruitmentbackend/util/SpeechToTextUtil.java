package com.example.airecruitmentbackend.util;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 讯飞语音转文字工具类
 * 基于讯飞RAASR API实现语音识别功能
 */
@Slf4j
public class SpeechToTextUtil {
    // 讯飞API主机地址
    private static final String HOST = "https://raasr.xfyun.cn";
    
    // 应用ID（需要替换为实际值）
    private static final String APP_ID = System.getenv("XF_APP_ID");
    
    // 密钥（需要替换为实际值）
    private static final String KEY_SECRET = System.getenv("XF_KEY_SECRET");
    
    // Gson实例
    private static final Gson gson = new Gson();
    
    /**
     * 同步识别音频文件
     * @param audioFilePath 音频文件路径
     * @return 识别结果
     * @throws Exception 异常信息
     */
    public static String recognizeSync(String audioFilePath) throws Exception {
        log.info("开始同步识别音频文件: {}", audioFilePath);
        
        // 上传音频文件
        String uploadResult = upload(audioFilePath);
        String jsonStr = StringEscapeUtils.unescapeJavaScript(uploadResult);
        String orderId = String.valueOf(JSONUtil.getByPath(JSONUtil.parse(jsonStr), "content.orderId"));
        log.info("音频上传成功，orderId: {}", orderId);
        
        // 获取识别结果
        String result = extractFromLattice(getResult(orderId));
        log.info("语音识别完成，结果长度: {}", result.length());
        
        return result;
    }
    
    /**
     * 异步识别音频文件
     * @param audioFilePath 音频文件路径
     * @return CompletableFuture<String> 识别结果
     */
    public static CompletableFuture<String> recognizeAsync(String audioFilePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return recognizeSync(audioFilePath);
            } catch (Exception e) {
                log.error("异步识别音频文件失败: {}", e.getMessage(), e);
                throw new RuntimeException("语音识别失败", e);
            }
        });
    }
    
    /**
     * 上传音频文件到讯飞服务器
     * @param audioFilePath 音频文件路径
     * @return 上传结果
     * @throws SignatureException 签名异常
     * @throws FileNotFoundException 文件未找到异常
     */
    private static String upload(String audioFilePath) throws SignatureException, FileNotFoundException {
        HashMap<String, Object> map = new HashMap<>(16);
        File audio = new File(audioFilePath);
        
        if (!audio.exists()) {
            throw new FileNotFoundException("音频文件不存在: " + audioFilePath);
        }
        
        String fileName = audio.getName();
        long fileSize = audio.length();
        
        map.put("appId", APP_ID);
        map.put("fileSize", fileSize);
        map.put("fileName", fileName);
        map.put("duration", "20000"); // 默认时长
        
        // 生成签名
        LfasrSignature lfasrSignature = new LfasrSignature(APP_ID, KEY_SECRET);
        map.put("signa", lfasrSignature.getSigna());
        map.put("ts", lfasrSignature.getTs());
        
        String paramString = HttpUtil.parseMapToPathParam(map);
        log.debug("上传参数: {}", paramString);
        
        String url = HOST + "/v2/api/upload" + "?" + paramString;
        log.debug("上传URL: {}", url);
        
        String response = HttpUtil.iflyrecUpload(url, new FileInputStream(audio));
        log.debug("上传响应: {}", response);
        
        return response;
    }
    
    /**
     * 获取识别结果
     * @param orderId 订单ID
     * @return 识别结果
     * @throws SignatureException 签名异常
     * @throws InterruptedException 中断异常
     * @throws IOException IO异常
     */
    private static String getResult(String orderId) throws SignatureException, InterruptedException, IOException {
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("orderId", orderId);
        
        // 生成签名
        LfasrSignature lfasrSignature = new LfasrSignature(APP_ID, KEY_SECRET);
        map.put("signa", lfasrSignature.getSigna());
        map.put("ts", lfasrSignature.getTs());
        map.put("appId", APP_ID);
        
        String paramString = HttpUtil.parseMapToPathParam(map);
        String url = HOST + "/v2/api/getResult" + "?" + paramString;
        log.debug("获取结果URL: {}", url);
        
        // 轮询获取结果
        while (true) {
            String response = HttpUtil.iflyrecGet(url);
            JsonParse jsonParse = gson.fromJson(response, JsonParse.class);
            
            if (jsonParse.content.orderInfo.status == 4) {
                // 识别完成
                log.info("识别完成，订单状态: {}", jsonParse.content.orderInfo.status);
                return response;
            } else if (jsonParse.content.orderInfo.status == -1) {
                // 识别失败
                log.error("识别失败，订单状态: {}", jsonParse.content.orderInfo.status);
                throw new RuntimeException("语音识别失败: " + response);
            } else {
                // 识别中，继续轮询
                log.debug("识别中，订单状态: {}", jsonParse.content.orderInfo.status);
                Thread.sleep(2000); // 2秒轮询一次
            }
        }

    }

    /**
     * 从lattice提取顺滑结果（最优方案）
     */
    public static String extractFromLattice(String apiResponse) {
        try {
            // 1. 解析完整的API响应
            JsonObject responseJson = gson.fromJson(apiResponse, JsonObject.class);

            // 检查响应码
            String code = responseJson.get("code").getAsString();
            if (!"000000".equals(code)) {
                log.error("API调用失败: {}", responseJson.get("descInfo").getAsString());
                return "API调用失败";
            }

            // 2. 获取content.orderResult
            JsonObject content = responseJson.getAsJsonObject("content");
            if (content == null || !content.has("orderResult")) {
                return "无识别结果";
            }

            String orderResultStr = content.get("orderResult").getAsString();

            // 3. 解析orderResult
            JsonObject orderResult = gson.fromJson(orderResultStr, JsonObject.class);
            StringBuilder result = new StringBuilder();

            // 4. 优先检查 lattice
            if (orderResult.has("lattice")) {
                JsonArray lattice = orderResult.getAsJsonArray("lattice");

                for (JsonElement segment : lattice) {
                    JsonObject seg = segment.getAsJsonObject();

                    // lattice中的json_1best是字符串
                    String json1bestStr = seg.get("json_1best").getAsString();
                    JsonObject json1best = gson.fromJson(json1bestStr, JsonObject.class);

                    result.append(extractTextFromJson1best(json1best));
                }
                return result.toString().trim();
            }
            // 如果没有lattice，检查lattice2
            else if (orderResult.has("lattice2")) {
                JsonArray lattice2 = orderResult.getAsJsonArray("lattice2");

                for (JsonElement segment : lattice2) {
                    JsonObject seg = segment.getAsJsonObject();
                    JsonObject json1best = seg.getAsJsonObject("json_1best");
                    result.append(extractTextFromJson1best(json1best));
                }
                return result.toString().trim();
            } else {
                return "无识别结果";
            }

        } catch (Exception e) {
            log.error("解析失败: {}", e.getMessage(), e);
            return "解析失败: " + e.getMessage();
        }
    }

    private static String extractTextFromJson1best(JsonObject json1best) {
        StringBuilder text = new StringBuilder();

        if (json1best.has("st")) {
            JsonObject st = json1best.getAsJsonObject("st");
            JsonArray rtArray = st.getAsJsonArray("rt");

            for (JsonElement rtElement : rtArray) {
                JsonObject rt = rtElement.getAsJsonObject();
                JsonArray wsArray = rt.getAsJsonArray("ws");

                for (JsonElement wsElement : wsArray) {
                    JsonObject ws = wsElement.getAsJsonObject();
                    JsonArray cwArray = ws.getAsJsonArray("cw");

                    for (JsonElement cwElement : cwArray) {
                        JsonObject cw = cwElement.getAsJsonObject();
                        String word = cw.get("w").getAsString();

                        if (!word.isEmpty()) {
                            text.append(word);
                        }
                    }
                }
            }
        }

        return text.toString();
    }
    /**
     * 内部类：解析JSON响应
     */
    static class JsonParse {
        String code;
        String descInfo;
        Content content;
    }

    static class Content {
        OrderInfo orderInfo;
        String orderResult;  // 新增：识别结果字符串
        Integer taskEstimateTime;  // 新增：预估耗时
    }

    static class OrderInfo {
        String orderId;
        Integer failType;
        Integer status;  // 关键：4表示完成，-1表示失败
        Long originalDuration;
        Long realDuration;
    }
}
