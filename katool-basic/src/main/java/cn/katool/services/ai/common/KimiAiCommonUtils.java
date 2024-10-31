package cn.katool.services.ai.common;

import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatRequest;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
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
    // 模型自动升级策略
    public static KimiChatRequest upgrade(List<String>keys,KimiChatRequest kimiChatRequest,String maxModel) {
        try{
            String model = kimiChatRequest.getModel();
            // 获取当前模型支持的最大token
            Long maxToken = KimiAiCommonUtils.getMaxToken(model);
            // 用一个新的模型来询问已经使用的token数量
            Long aLong = KimiAIServiceFactory.createKimiAiService(KimiModel.MOONSHOT_V1_8K,null,null,false)
                    .setKeys(keys).setChatRequest(kimiChatRequest)
                    .countToken(kimiChatRequest.getMessages());
            // 如果说超出了模型限制，那么进行模型升级
            if (maxToken < aLong || kimiChatRequest.getMax_tokens() == maxToken - aLong) {
                String nextModel = KimiAiCommonUtils.getNextModel(model);
                if (null!=maxModel && nextModel.equals(maxModel)){
                    throw new KaToolException(ErrorCode.AI_UPGRADE_ERROR,"已达到最大限制，模型升级失败，请开启新的会话 或者 进行总结");
                }
                Long nextMaxToken = KimiAiCommonUtils.getMaxToken(nextModel);
                // 如果是超级加倍了，那么继续
                if (nextMaxToken < aLong) {
                    nextModel = KimiAiCommonUtils.getNextModel(nextModel);
                    nextMaxToken = KimiAiCommonUtils.getMaxToken(nextModel);
                }
                log.warn("KimiAiService-Model[upgrade = plus:{}] request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",nextModel,aLong,kimiChatRequest.getMax_tokens(),maxToken,nextMaxToken);
                kimiChatRequest.setModel(nextModel);
                kimiChatRequest.setMax_tokens(nextMaxToken-aLong);
            }
            else{
                log.warn("KimiAiService-Model[upgrade = local:{}] request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",model,aLong,kimiChatRequest.getMax_tokens(),maxToken,maxToken-aLong);
                kimiChatRequest.setMax_tokens(maxToken-aLong);
            }
        } catch (KaToolException e) {
            if (e.getCode() == ErrorCode.AI_UPGRADE_ERROR.getCode()){
                // 对于聊天内容进行主旨内容提取
                List<CommonAIMessage> messages = kimiChatRequest.getMessages();
                List<CommonAIMessage> list = messages.subList(1, messages.size());
                StringBuffer prompt = new StringBuffer();list.stream().filter(v -> v.getRole().equals(CommonAIRoleEnum.ASSISTANT.getRole()))
                        .map(v -> v.getContent()).collect(Collectors.toList()).forEach(item ->{
                            prompt.append("[ BACK ]:(");
                            prompt.append(item);
                            prompt.append(")\n");
                        });
                String backtemplate = prompt.toString();

                HashMap<String, String> insteadMapping = new HashMap<>();
                insteadMapping.put("historyStory", backtemplate);
                PromptTemplateDrive promptTemplateDrive = PromptTemplateDrive.create("As a professional paper summarization expert, please summarize the following content using the format:\n" +
                        "\n" +
                        "\\[ BACK \\](content)\n" +
                        "\n" +
                        "In this format, each \\[BACK\\] represents a result, and the content within the parentheses is the main content.\n" +
                        "\n" +
                        "Below is the chat content. Please provide only the main content without any additional commentary:\n" +
                        "\n" +
                        "${historyStory}", insteadMapping);
                // 对上下文总结的数据
                String finalData = KimiAIServiceFactory.createKimiAiService(KimiModel.MOONSHOT_V1_AUTO, promptTemplateDrive, null, false)
                        .setKeys(keys).ask("Please provide the chat content so I can summarize it in detail using the same language.");
                HashMap<String, String> genrouterInsteadMap = new HashMap<>();
                genrouterInsteadMap.put("history content", finalData);
                List<CommonAIMessage> userQuestions = messages.stream().filter(v -> v.getRole().equals(CommonAIRoleEnum.USER.getRole())).collect(Collectors.toList());
                genrouterInsteadMap.put("my question", userQuestions.get(userQuestions.size()-1).getContent());
                PromptTemplateDrive generouter = PromptTemplateDrive.create("PAside from the previous prompts, we have already engaged in some conversations. Due to the extensive content, I asked a recorder to summarize the context so we can realign our understanding. Below is the main content of our previous discussion:\n" +
                        "\n" +
                        "[ BACK ]( ${history content} )\n" +
                        "\n" +
                        "Please use the language from the last conversation to respond to the message I initiated.\n" +
                        "\n" +
                        "Please tell me of ${my question}", genrouterInsteadMap);
                CommonAIMessage commonAIMessage = generouter.generateTemplate();
                commonAIMessage.setRole(CommonAIRoleEnum.USER.getRole());
                CommonAIMessage commonAIMessage1 = messages.get(0);
                messages.clear();
                messages.add(commonAIMessage1);
                messages.add(commonAIMessage);
                kimiChatRequest.setMessages(messages);
                kimiChatRequest.setModel(KimiModel.MOONSHOT_V1_8K);
                kimiChatRequest.setMax_tokens(getMaxToken(kimiChatRequest.getModel()));
//                kimiChatRequest.setMax_tokens()
            }
        }
        // 策略优化：上下文总结
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
                return KimiModel.MOONSHOT_V1_AUTO;
            default:
                throw new KaToolException(ErrorCode.AI_UPGRADE_ERROR,"已达到最大限制，模型升级失败，请开启新的会话 或者 进行总结");
        }
    }
}
