package cn.katool.services.ai.common;

import cn.katool.services.ai.constant.kimi.KimiModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KimiAiUtils {
    public static Long getIniterToken(String kimoModel) {
        Long maxToken = 1024L;
        switch (kimoModel) {
            case KimiModel.MOONSHOT_V1_8K:
                maxToken = 4*1024L;
                break;
            case KimiModel.MOONSHOT_V1_32K:
                maxToken = 16*1024L;
                break;
            case KimiModel.MOONSHOT_V1_128K:
                maxToken = 64*1024L;
                break;
            default:
                maxToken = 4*1024L;
        }
        return maxToken;
    }

    public static Long getMaxToken(String kimoModel) {
        Long maxToken = 8*1024L;
        switch (kimoModel) {
            case KimiModel.MOONSHOT_V1_8K:
                maxToken = 8*1024L;
                break;
            case KimiModel.MOONSHOT_V1_32K:
                maxToken = 32*1024L;
                break;
            case KimiModel.MOONSHOT_V1_128K:
                maxToken = 128*1024L;
                break;
            default:
                maxToken = 8*1024L;
        }
        return maxToken;
    }

    public static String getNextModel(String model) {
        switch (model) {
            case KimiModel.MOONSHOT_V1_8K:
                log.warn("当前会话已经达到第一次限制提醒");
                return KimiModel.MOONSHOT_V1_32K;
            case KimiModel.MOONSHOT_V1_32K:
                log.warn("当前会话已经达到第二次限制提醒，请尽快完成使用，第三次将强制退出");
                return KimiModel.MOONSHOT_V1_128K;
            case KimiModel.MOONSHOT_V1_128K:
                log.warn("当前对话次数已到达最大限制，请重新启动程序进行下一轮会话");
            default:
                throw new RuntimeException("已达到最大限制，模型升级失败，请开启新的会话");
        }
    }
}
