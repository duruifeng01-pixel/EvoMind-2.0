package com.evomind.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 语音转文字服务接口
 * 提供语音识别和转写功能
 */
public interface VoiceTranscriptionService {

    /**
     * 同步转写语音文件
     * 上传音频文件并获取转写结果
     *
     * @param audioFile 音频文件
     * @return 转写后的文本
     */
    String transcribeSync(MultipartFile audioFile);

    /**
     * 同步转写语音文件（带格式参数）
     *
     * @param audioFile 音频文件
     * @param format 音频格式 (pcm/wav/amr/m4a)
     * @param sampleRate 采样率 (16000/8000)
     * @return 转写后的文本
     */
    String transcribeSync(MultipartFile audioFile, String format, int sampleRate);

    /**
     * 使用Base64音频数据转写
     *
     * @param audioBase64 Base64编码的音频数据
     * @param format 音频格式
     * @param sampleRate 采样率
     * @return 转写后的文本
     */
    String transcribeFromBase64(String audioBase64, String format, int sampleRate);

    /**
     * 获取访问Token
     * 用于客户端直接调用百度语音API
     *
     * @return Access Token
     */
    String getAccessToken();

    /**
     * 检查服务是否可用
     *
     * @return true if available
     */
    boolean isAvailable();
}
