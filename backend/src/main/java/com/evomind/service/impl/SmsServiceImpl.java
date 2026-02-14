package com.evomind.service.impl;

import com.evomind.config.SmsConfig;
import com.evomind.service.SmsService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SmsServiceImpl implements SmsService {

    private final SmsConfig smsConfig;

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        String templateParam = String.format("{\"code\":\"%s\"}", code);
        return sendSms(phone, smsConfig.getVerificationTemplateId(), templateParam);
    }

    @Override
    public boolean sendSms(String phone, String templateCode, String params) {
        try {
            Credential cred = new Credential(smsConfig.getSecretId(), smsConfig.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(smsConfig.getAppId());
            req.setSignName(smsConfig.getSignName());
            req.setTemplateId(templateCode);
            req.setPhoneNumberSet(new String[]{"+86" + phone});
            req.setTemplateParamSet(new String[]{params});

            SendSmsResponse resp = client.SendSms(req);
            log.info("短信发送结果: {}", SendSmsResponse.toJsonString(resp));

            return resp.getSendStatusSet() != null && 
                   resp.getSendStatusSet().length > 0 &&
                   "Ok".equals(resp.getSendStatusSet()[0].getCode());
        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
