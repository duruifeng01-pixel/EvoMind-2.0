package com.evomind.service;

import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.OcrResultResponse;

/**
 * OCR服务接口
 * 提供图片文字识别功能
 */
public interface OcrService {

    /**
     * 识别图片中的博主/账号信息
     *
     * @param userId  用户ID
     * @param request OCR请求
     * @return 识别结果
     */
    OcrResultResponse recognizeBloggers(Long userId, OcrImportRequest request);

    /**
     * 通用文字识别
     *
     * @param imageBase64 Base64编码的图片
     * @param detectDirection 是否检测文字方向
     * @return 识别结果
     */
    String recognizeText(String imageBase64, boolean detectDirection);

    /**
     * 高精度文字识别（含位置信息）
     *
     * @param imageBase64 Base64编码的图片
     * @return 带位置信息的文字识别结果
     */
    OcrResultResponse recognizeTextWithLocation(String imageBase64);

    /**
     * 根据任务ID获取识别结果
     *
     * @param taskId 任务ID
     * @return 识别结果
     */
    OcrResultResponse getResultByTaskId(String taskId);

    /**
     * 解析小红书截图
     * 识别关注列表、推荐列表等
     *
     * @param imageBase64 Base64编码的图片
     * @return 识别出的博主列表
     */
    OcrResultResponse parseXiaohongshuScreenshot(String imageBase64);

    /**
     * 解析微信公众号截图
     *
     * @param imageBase64 Base64编码的图片
     * @return 识别出的公众号列表
     */
    OcrResultResponse parseWechatScreenshot(String imageBase64);
}
