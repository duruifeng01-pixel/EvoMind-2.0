package com.evomind.service;

import java.util.Map;

/**
 * 隐私与数据权利服务接口
 */
public interface PrivacyService {

    /**
     * 导出用户所有数据
     * @param userId 用户ID
     * @return 用户数据Map
     */
    Map<String, Object> exportUserData(Long userId);

    /**
     * 申请账号注销
     * @param userId 用户ID
     * @return 注销令牌
     */
    String requestAccountDeletion(Long userId);

    /**
     * 确认注销账号
     * @param userId 用户ID
     * @param deletionToken 注销令牌
     */
    void confirmAccountDeletion(Long userId, String deletionToken);

    /**
     * 取消注销申请
     * @param userId 用户ID
     */
    void cancelAccountDeletion(Long userId);

    /**
     * 获取数据导出历史
     * @param userId 用户ID
     * @return 导出历史
     */
    Map<String, Object> getExportHistory(Long userId);
}
