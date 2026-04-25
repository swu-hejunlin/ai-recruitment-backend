package com.example.airecruitmentbackend.util;

import java.security.SignatureException;

/**
 * Lfasr能力签名实体
 * 基于讯飞官方demo实现
 */
public class LfasrSignature extends AbstractSignature {

    /**
     * 构造方法
     * @param appId 应用ID
     * @param keySecret 密钥
     */
    public LfasrSignature(String appId, String keySecret) {
        super(appId, keySecret, null);
    }

    @Override
    public String getSigna() throws SignatureException {
        if (this.signa == null || this.signa.isEmpty()) {
            this.setOriginSign(generateOriginSign());
            this.signa = generateSignature();
        }
        return this.signa;
    }

    /**
     * 生成最终的签名，需要先生成原始sign
     * @return 签名结果
     * @throws SignatureException 签名异常
     */
    public String generateSignature() throws SignatureException {
        return CryptTools.hmacEncrypt(CryptTools.HMAC_SHA1, this.getOriginSign(), this.getKey());
    }

    /**
     * 生成待加密原始字符
     * @return 原始签名字符串
     * @throws SignatureException 签名异常
     */
    @Override
    public String generateOriginSign() throws SignatureException {
        return CryptTools.md5Encrypt(this.getId() + this.getTs());
    }
}
