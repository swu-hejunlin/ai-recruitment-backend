package com.example.airecruitmentbackend.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class PdfUtil {

    /**
     * 将PDF的第一页转换为Base64编码的PNG图片
     */
    public static String convertPdfFirstPageToBase64(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // 0 表示第一页，150 是 DPI（分辨率平衡清晰度和体积）
            BufferedImage image = renderer.renderImageWithDPI(0, 150);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("PDF解析失败", e);
        }
    }
    /**
     * 从PDF文件中提取全部文本内容
     *
     * @param pdfBytes PDF文件的字节数组
     * @return 提取的文本内容
     * @throws RuntimeException 如果PDF解析失败
     */
    public static String extractTextFromPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // 设置按位置排序，提高可读性
            stripper.setSortByPosition(true);
            // 保留更多格式信息
            stripper.setAddMoreFormatting(true);

            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("PDF文本提取失败", e);
        }
    }
    /**
     * 从PDF文件中提取文本并进行清理优化
     * 适用于简历、文档等需要结构化的场景
     *
     * @param pdfBytes PDF文件的字节数组
     * @return 清理后的文本内容
     * @throws RuntimeException 如果PDF解析失败
     */
    public static String extractAndCleanText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(true);

            String rawText = stripper.getText(document);
            return cleanExtractedText(rawText);
        } catch (Exception e) {
            throw new RuntimeException("PDF文本提取与清理失败", e);
        }
    }
    /**
     * 清理提取的文本
     * 1. 移除多余的空行和空格
     * 2. 修复常见的编码问题
     * 3. 合并连续的换行
     */
    private static String cleanExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 1. 移除多余的空行（保留最多连续两个换行）
        text = text.replaceAll("(?m)^[ \\t]*\\r?\\n", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");

        // 2. 移除行首行尾的空白字符
        text = text.replaceAll("(?m)^\\s+|\\s+$", "");

        // 3. 合并连续的空白字符
        text = text.replaceAll("\\s{2,}", " ");

        // 4. 修复常见的全角/半角空格问题
        text = text.replace("　", " ")  // 全角空格
                .replace(" ", " ")   // 窄空格
                .replace(" ", " ");  // 不换行空格

        return text.trim();
    }

}
