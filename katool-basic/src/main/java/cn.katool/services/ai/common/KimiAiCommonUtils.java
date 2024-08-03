package cn.katool.services.ai.common;

import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.config.ai.kimi.KimiConfig;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
public class KimiAiCommonUtils {
    public static String getKeyByCircleQueue(List<String> keyList, Queue<String> keyCircleQueue){
        List<String> reduce_key = keyList.stream().filter(o -> !keyCircleQueue.contains(o)).collect(Collectors.toList());
        List<String> expire_key = keyCircleQueue.stream().filter(o -> !keyList.contains(o)).collect(Collectors.toList());
        if (keyCircleQueue.isEmpty()||keyCircleQueue.size()!=keyList.size()||!reduce_key.isEmpty()) {
            // init to map
            if (keyList.isEmpty()) {
                throw new KaToolException(ErrorCode.PARAMS_ERROR, "请设置KimiAI-key");
            }
            for (String s : reduce_key) {
                keyCircleQueue.add(s);
            }
        }
        if (!expire_key.isEmpty()){
            keyCircleQueue.removeIf(v->expire_key.contains(v));
        }
        String t1 = "";
        synchronized (keyCircleQueue) {
            t1 = keyCircleQueue.poll();
            keyCircleQueue.offer(t1);
        }
        return t1;
    }
    public static KimiChatRequest upgrade(List<String>keys,KimiChatRequest kimiChatRequest) {
        String model = kimiChatRequest.getModel();
        Long maxToken = KimiAiCommonUtils.getMaxToken(model);
        Long aLong = KimiAIServiceFactory.createKimiAiService(KimiModel.MOONSHOT_V1_8K,null,null,false).setKeys(keys).setChatRequest(kimiChatRequest).countToken(kimiChatRequest.getMessages());
        if (maxToken < aLong) {
            String nextModel = KimiAiCommonUtils.getNextModel(model);
            Long nextMaxToken = KimiAiCommonUtils.getMaxToken(nextModel);
            if (nextMaxToken < aLong) {
                nextModel = KimiAiCommonUtils.getNextModel(nextModel);
                nextMaxToken = KimiAiCommonUtils.getMaxToken(nextModel);
            }
            log.warn("KimiAiService-Model[upgrade] request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",aLong,kimiChatRequest.getMax_tokens(),maxToken,nextMaxToken);
            kimiChatRequest.setModel(nextModel);
            kimiChatRequest.setMax_tokens(nextMaxToken-aLong);
        }
        else{
            log.warn("KimiAiService-Model[upgrade] request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",aLong,kimiChatRequest.getMax_tokens(),maxToken,maxToken-aLong);
            kimiChatRequest.setMax_tokens(maxToken-aLong);
        }
        return kimiChatRequest;
    }
    public static Long getIniterToken(String kimiModel) {
        Long maxToken = 1024L;
        switch (kimiModel) {
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

    public static Long getMaxToken(String kimiModel) {
        Long maxToken = 8*1024L;
        switch (kimiModel) {
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
                return KimiModel.MOONSHOT_V1_32K;
            case KimiModel.MOONSHOT_V1_32K:
                return KimiModel.MOONSHOT_V1_128K;
            case KimiModel.MOONSHOT_V1_128K:
            default:
                throw new RuntimeException("已达到最大限制，模型升级失败，请开启新的会话");
        }
    }
}
