package com.example.airecruitmentbackend.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SignatureException;

/**
 * 加签加密抽象类
 * 基于讯飞官方demo实现
 */
public abstract class AbstractSignature {
    /**
     * 签名ID
     */
    private String id;

    /**
     * 加密key
     */
    private String key;

    /**
     * 服务url
     */
    private String url;

    /**
     * 加密算法
     */
    private String encryptType;

    /**
     * 待加密原始字符
     */
    private String originSign;

    /**
     * 最终生成的签名
     */
    protected String signa;

    /**
     * 时间戳timestamp
     */
    private String ts;

    /**
     * 请求类型，默认get
     */
    protected String requestMethod = "GET";

    /**
     * 构造方法
     * @param id 签名ID
     * @param key 加密key
     * @param url 服务url
     */
    public AbstractSignature(String id, String key, String url) {
        this.id = id;
        this.key = key;
        this.url = url;
        this.ts = generateTs();
    }

    /**
     * 构造方法
     * @param id 签名ID
     * @param key 加密key
     * @param url 服务url
     * @param isPost 是否为POST
     */
    public AbstractSignature(String id, String key, String url, boolean isPost) {
        this.id = id;
        this.key = key;
        this.url = url;
        if (isPost) {
            this.requestMethod = "POST";
        }else{
            this.requestMethod = "GET";
        }

        this.ts = generateTs();
    }

    /**
     * 生成ts时间
     * @return 时间戳
     */
    public String generateTs() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }

    /**
     * 完成签名，返回完整签名
     * @return 签名结果
     * @throws SignatureException 签名异常
     */
    public abstract String getSigna() throws SignatureException;

    /**
     * 生成待加密原始字符
     * @return 原始签名字符串
     * @throws SignatureException 签名异常
     */
    public String generateOriginSign() throws SignatureException {
        try {
            URL url = new URL(this.getUrl());

            return "host: " + url.getHost() + "\n" +
                    "date: " + this.getTs() + "\n" +
                    "GET " + url.getPath() + " HTTP/1.1";
        } catch (MalformedURLException e) {
            throw new SignatureException("MalformedURLException:" + e.getMessage());
        }
    }

    /**
     * 获取签名ID
     * @return 签名ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置签名ID
     * @param id 签名ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取加密key
     * @return 加密key
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置加密key
     * @param key 加密key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获取待加密原始字符
     * @return 待加密原始字符
     */
    public String getOriginSign() {
        return originSign;
    }

    /**
     * 设置待加密原始字符
     * @param originSign 待加密原始字符
     */
    public void setOriginSign(String originSign) {
        this.originSign = originSign;
    }

    /**
     * 获取时间戳
     * @return 时间戳
     */
    public String getTs() {
        return ts;
    }

    /**
     * 设置时间戳
     * @param ts 时间戳
     */
    public void setTs(String ts) {
        this.ts = ts;
    }

    /**
     * 获取服务url
     * @return 服务url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置服务url
     * @param url 服务url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取加密算法
     * @return 加密算法
     */
    public String getEncryptType() {
        return encryptType;
    }

    /**
     * 设置加密算法
     * @param encryptType 加密算法
     */
    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }
}
