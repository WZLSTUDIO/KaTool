package cn.katool.services.ai.common;
import cn.hutool.core.lang.Pair;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatRequest;
import cn.katool.services.ai.model.dto.kimi.tools.KimiToolBody;
import cn.katool.services.ai.model.dto.kimi.tools.functions.KimiFunctionBody;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.KimiToolParameters;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.services.ai.server.kimi.KimiAIService;
import org.springframework.util.ObjectUtils;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 部分构建可以使用KimiTuples工具来构建
 */
public class KimiAIServiceFactory {
    private static KimiChatRequest createKimiChatRequest(String kimiModel,List<KimiToolBody> tools) {
        KimiChatRequest kimiChatRequest = new KimiChatRequest();
        kimiChatRequest.setModel(kimiModel)
                .setMax_tokens(KimiAiCommonUtils.getIniterToken(kimiModel));
        kimiChatRequest.setTools(tools);
        return kimiChatRequest;
    }
    private static KimiAIService createEmptyService(PromptTemplateDrive promptTemplateDrive, Set<Tuple3<String, String, Function<Map<String, String>, String>>> toolsConfigKey){
        KimiAIService kimiAIService = new KimiAIService();
        if (null != promptTemplateDrive) {
            kimiAIService.setPromptTemplateDrive(promptTemplateDrive);
        }
        HashMap<String, Function<Map<String, String>, String>> kimiFunctionDriverMap = new HashMap<>();
        Optional.ofNullable(toolsConfigKey).ifPresent(item->item.forEach(v->{
            String methodName = v.getT1();
            Function<Map<String, String>, String> function = v.getT3();
            if (null == function){
                return ;
            }
            kimiFunctionDriverMap.put(methodName,function);
        }));
        if (!kimiFunctionDriverMap.isEmpty()) {
            kimiAIService.setKimiFunctionDriverMap(kimiFunctionDriverMap);
        }
        return kimiAIService;
    }


    public static <T>KimiAIService createDefualtKimiAiService(String kimiModel,
                                                              PromptTemplateDrive promptTemplateDrive,
                                                              Map<Tuple3<String,String,Function<Map<String,String>,String>>,List<Tuple3<String,String,T>>> toolsConfig) {
        return createDefualtKimiAiService(kimiModel,promptTemplateDrive,toolsConfig,true);
    }
    public static <T>KimiAIService createDefualtKimiAiService(String kimiModel,
                                                           PromptTemplateDrive promptTemplateDrive,
                                                           Map<Tuple3<String,String,Function<Map<String,String>,String>>,List<Tuple3<String,String,T>>> toolsConfig,Boolean autoUpgrate) {
        KimiAIService kimiAIService = createEmptyService(promptTemplateDrive, null!=toolsConfig?toolsConfig.keySet():null);
        KimiChatRequest kimiChatRequest = createKimiChatRequest(kimiModel,getToolsDefault(toolsConfig));
        kimiAIService.setChatRequest(kimiChatRequest).setEnableAutoUpgrade(autoUpgrate);
        return kimiAIService;
    }
    public static <T> KimiAIService createKimiAiService(String kimiModel,
                                                        PromptTemplateDrive promptTemplateDrive,
                                                        Map<Tuple3<String,String,Function<Map<String,String>,String>>,List<Tuple4<String,String,T,String>>> toolsConfig) {
        return createKimiAiService(kimiModel,promptTemplateDrive,toolsConfig,true);
    }
    public static <T> KimiAIService createKimiAiService(String kimiModel,
                                                    PromptTemplateDrive promptTemplateDrive,
                                                    Map<Tuple3<String,String,Function<Map<String,String>,String>>,List<Tuple4<String,String,T,String>>> toolsConfig,Boolean autoUpgrate) {
        KimiAIService kimiAIService = createEmptyService(promptTemplateDrive,null!=toolsConfig?toolsConfig.keySet():null);
        KimiChatRequest kimiChatRequest = createKimiChatRequest(kimiModel,getTools(toolsConfig));
        kimiAIService.setChatRequest(kimiChatRequest).setEnableAutoUpgrade(autoUpgrate);
        return kimiAIService;
    }
    private static <T> KimiToolParameters getSearchToolParametersDefaultType(List<Tuple3<String,String,T>> paramsAndSecuma) {
        KimiToolParameters kimiToolParameters = new KimiToolParameters();
        Map<String,KimiToolParametersPropertiesValue> paramsSecumaMap = paramsAndSecuma.stream()
                .collect(Collectors.toMap(v -> v.getT1(), v ->
                        new KimiToolParametersPropertiesValue<T>().setSchema(
                                        Pair.of(v.getT2(), v.getT3())
                                )));
        List<String> params =paramsSecumaMap.keySet().stream().collect(Collectors.toList());;
        kimiToolParameters.setRequired(params).setProperties(paramsSecumaMap);
        return kimiToolParameters;
    }
    private static <T> KimiToolParameters getSearchToolParameters(List<Tuple4<String,String,T,String>> paramsAndSecuma) {
        KimiToolParameters kimiToolParameters = new KimiToolParameters();
        Map<String,KimiToolParametersPropertiesValue> paramsSecumaMap = paramsAndSecuma.stream()
                .collect(Collectors.toMap(v -> v.getT1(), v ->
                        new KimiToolParametersPropertiesValue<T>()
                                        .setType(v.getT4()).setSchema(
                                        Pair.of(v.getT2(), v.getT3())
                                )));
        List<String> params =paramsSecumaMap.keySet().stream().collect(Collectors.toList());;
        kimiToolParameters.setRequired(params).setProperties(paramsSecumaMap);
        return kimiToolParameters;
    }
    private static <T> KimiFunctionBody createKimiFunctionBodyDefaultType(String name, String description,List<Tuple3<String,String,T>> paramsAndSecuma) {
        return new KimiFunctionBody()
                .setName(name)
                .setDescription(description)
                .setParameters(getSearchToolParametersDefaultType(paramsAndSecuma));
    }
    private static <T> KimiFunctionBody createKimiFunctionBody(String name, String description,List<Tuple4<String,String,T,String>> paramsAndSecuma) {
        return new KimiFunctionBody()
                .setName(name)
                .setDescription(description)
                .setParameters(getSearchToolParameters(paramsAndSecuma));
    }
    private static <T> List<KimiToolBody> getToolsDefault(Map<Tuple3<String,String, Function<Map<String,String>,String>>,List<Tuple3<String,String,T>>> toolsConfig) {
        if (ObjectUtils.isEmpty(toolsConfig)) {
            return null;
        }
        List<KimiToolBody> collect = toolsConfig.entrySet().stream().map(v -> {
            return new KimiToolBody()
                    .setFunction(createKimiFunctionBodyDefaultType(v.getKey().getT1(), v.getKey().getT2(), v.getValue()));
        }).collect(Collectors.toList());
        return collect;
    }
    private static <T> List<KimiToolBody> getTools(Map<Tuple3<String,String,Function<Map<String,String>,String>>,List<Tuple4<String,String,T,String>>> toolsConfig) {
        if (ObjectUtils.isEmpty(toolsConfig)) {
            return null;
        }
        List<KimiToolBody> collect = toolsConfig.entrySet().stream().map(v -> {
            return new KimiToolBody()
                    .setFunction(createKimiFunctionBody(v.getKey().getT1(), v.getKey().getT2(), v.getValue()));
        }).collect(Collectors.toList());
        return collect;
    }
}
