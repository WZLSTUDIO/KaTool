package cn.katool.services.ai.common;

import cn.hutool.json.JSONUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.kimi.KimiMessageFinishResonConstants;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatResponse;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiMergeMessage;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiToolCallsMessage;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCalls;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCallsFuntion;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import cn.katool.services.ai.model.entity.kimi.KimiError;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

public class KimiEventSourceLinsener extends EventSourceListener {


    private String upRole;
    private StringBuilder localContent = new StringBuilder("");
    private StringBuilder json = new StringBuilder("");

    private Consumer<String> kimiAiResponseDetail = null;
    private List<CommonAIMessage> history = new ArrayList<>();

    private Callable<EventSource> callBack = null;
    volatile private CountDownLatch eventLatch = null;


    public KimiEventSourceLinsener(Consumer<String> kimiAiResponseDetail,Callable callBack,
                                   Map<String, Function<Map<String, String>, String>> kimiFunctionDriverMap, List<CommonAIMessage> history,
                                   CountDownLatch eventLatch) {
        this.kimiAiResponseDetail = kimiAiResponseDetail;
        this.callBack = callBack;
        this.kimiFunctionDriverMap = kimiFunctionDriverMap;
        this.history = history;
        this.eventLatch =eventLatch;
    }

    Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap = null;
    void addLocalContent(KimiChatResponse response) {
        KimiAiMergeMessage currentMeesge = response.getChoices().get(0).getDelta();
        String currentContent = currentMeesge.getContent();
        localContent.append(currentContent);;
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        eventLatch.countDown();
        super.onClosed(eventSource);
    }
    private List<KimiAiToolCallsMessage> messages = Arrays.asList(new KimiAiToolCallsMessage());
    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
        if (StringUtils.isNotBlank(json) && JSONUtil.isTypeJSON(json.toString())){
            KimiChatResponse response = KimiGsonFactory.create().fromJson(json.toString(), TypeToken.get(KimiChatResponse.class).getType());
            List<KimiChatResponse.Choice> choices = response.getChoices();
            for(KimiChatResponse.Choice choice:choices){
                int choiceIndex = choice.getIndex();
                KimiAiToolCallsMessage message = messages.get(choiceIndex);
                KimiChatResponse.Usage usage = choice.getUsage();
                message.setUsage(usage);
                KimiAiMergeMessage delta = choice.getDelta();
                if (null != delta){
                    String role = delta.getRole();
                    if (StringUtils.isNotBlank(role)){
                        if (StringUtils.isNotBlank(upRole)){
                            KimiAiMergeMessage msg = delta;
                            msg.setRole(upRole);
                            msg.setContent(localContent.toString());
                            history.add(msg);
                        }
                        message.setRole(role);
                        upRole = role;
                        localContent = new StringBuilder("");
                    }
                    String content = delta.getContent();
                    if (StringUtils.isNotBlank(content)){
                        message.setContent(message.getContent()+content);
                    }
                    List<ToolCalls> toolCalls = delta.getTool_calls();
                    if(toolCalls != null && toolCalls.size() > 0 && null != kimiFunctionDriverMap && !kimiFunctionDriverMap.isEmpty()) {
                        ArrayList<ToolCalls> toolCallBodys = new ArrayList<>();
                        message.setTool_calls(toolCallBodys);
                        toolCalls.forEach(toolCall-> {
                            Integer toolCallIndex = toolCall.getIndex();
                            int toolCallBodysSize = toolCallBodys.size();
                            if(toolCallBodysSize <= toolCallIndex+1){
                                for (int i = toolCallBodysSize; i <= toolCallIndex+1; i++) {
                                    toolCallBodys.add(new ToolCalls());
                                }
                            }
                            ToolCalls tool_call_object = toolCallBodys.get(toolCallIndex);
                            tool_call_object.setIndex(toolCallIndex);
                            String toolCallId = toolCall.getId();
                            if (StringUtils.isNotBlank(toolCallId)) {
                                tool_call_object.setId(toolCallId);
                            }
                            String toolCallType = toolCall.getType();
                            if (StringUtils.isNotBlank(toolCallType)){
                                tool_call_object.setType(toolCallType);
                            }
                            ToolCallsFuntion function = toolCall.getFunction();
                            if (function != null){
                                if(null == tool_call_object.getFunction()){
                                    tool_call_object.setFunction(new ToolCallsFuntion());
                                }
                                String functionName = function.getName();
                                if (StringUtils.isNotBlank(functionName)){
                                    tool_call_object.getFunction().setName(functionName);
                                }
                                String arguments = function.getArguments();
                                if (StringUtils.isNotBlank(arguments)){
                                    String orginArguments = toolCall.getFunction().getArguments();
                                    if (StringUtils.isBlank(orginArguments)){
                                        orginArguments = "";
                                    }
                                    tool_call_object.getFunction().setArguments(orginArguments +arguments);
                                }
                            }
                            message.getTool_calls().set(toolCallIndex, tool_call_object);
                        });
                    }else{
                        addLocalContent(response);
                        // 开发者对于每一条内容的处理
                        kimiAiResponseDetail.accept(content);
                    }
                }
            }
        }
        if ("[DONE]".equals(data) || (JSONUtil.isTypeJSON(data) &&
                KimiMessageFinishResonConstants.STOP.equals(((KimiChatResponse)KimiGsonFactory.create()
                        .fromJson(data,TypeToken.get(KimiChatResponse.class).getType()))
                        .getChoices().get(0).getFinish_reason()))){
            if (StringUtils.isNotBlank(upRole)){
                CommonAIMessage msg = new CommonAIMessage();
                msg.setRole(upRole);
                String content = localContent.toString();
                if (StringUtils.isBlank(content) && null != kimiFunctionDriverMap && !kimiFunctionDriverMap.isEmpty()){
                    messages.forEach(message->{
                        List<ToolCalls> toolCalls = message.getTool_calls();
                        for (ToolCalls toolCall : toolCalls) {
                            ToolCallsFuntion function = toolCall.getFunction();
                            if (null != function){
                                String name = function.getName();
                                String arguments = function.getArguments();
                                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(arguments)){
                                    String apply = kimiFunctionDriverMap.get(name).apply(new Gson().fromJson(arguments, Map.class));
                                    this.history.add(new CommonAIMessage().setRole(CommonAIRoleEnum.TOOL.getRole()).setContent(apply));
                                }
                            }

                        }
                    });
                    try {
                        callBack.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                msg.setContent(content);
                history.add(msg);
            }
            eventCancel(eventSource);
        }
        else if (data.startsWith("{\"id\":")){
            json = new StringBuilder(data);
        }
        else{
                json.append('\n').append(data);
        }

    }

    private void eventCancel(EventSource eventSource) {
        eventLatch.countDown();
        eventSource.cancel();
    }

    @SneakyThrows
    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        if (StringUtils.isNotBlank(upRole)){
            CommonAIMessage msg = new CommonAIMessage();
            msg.setRole(upRole);
            String content = localContent.toString();
            if (StringUtils.isBlank(content)){
                content = "不好意思,没有找到相关内容...";
            }
            msg.setContent(content);
            history.add(msg);
        }
        int code = response.code();
        if(code<200||code>299){
            eventCancel(eventSource);
            ResponseBody body = response.body();
            throw new KaToolException(ErrorCode.OPER_ERROR, KimiGsonFactory.create().fromJson(body.string(), KimiError.class).getError().getMessage());
        }
        super.onFailure(eventSource, t, response);
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        super.onOpen(eventSource, response);
    }


}
