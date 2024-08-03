package cn.katool.services.ai.server.kimi;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.common.SessionPackageTheadLocalAdaptor;
import cn.katool.config.ai.kimi.KimiConfig;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.common.KimiEventSourceLinsener;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.kimi.*;
import cn.katool.services.ai.model.builder.kimi.KimiBuilder;
import cn.katool.services.ai.model.builder.kimi.KimiCacheBuilder;
import cn.katool.services.ai.model.builder.kimi.KimiCacheRefsTagsBuilder;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseResponse;
import cn.katool.services.ai.model.dto.kimi.base.KimiDefaultDeleteResponse;
import cn.katool.services.ai.model.dto.kimi.cache.*;
import cn.katool.services.ai.model.dto.kimi.cache.tag.KimiCacheTagCreateRequest;
import cn.katool.services.ai.model.dto.kimi.cache.tag.KimiCacheTagMeta;
import cn.katool.services.ai.model.dto.kimi.cache.tag.KimiTagDeleteResponse;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatRequest;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileContentResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiMergeMessage;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiToolCallsMessage;
import cn.katool.services.ai.model.dto.kimi.other.KimiOtherResponse;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCalls;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCallsFuntion;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import cn.katool.services.ai.model.entity.RequestBody;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.services.ai.model.entity.kimi.KimiError;
import cn.katool.util.AiServiceHttpUtil;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.hutool.core.lang.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class KimiAIService implements CommonAIService<
        KimiError<KimiOtherResponse.KimiOtherResponseData>,
        KimiError<KimiFileMeta>,
        KimiError<KimiChatRequest>
        > {
    @Getter
    @Setter
    private volatile Proxy proxy = null;
    @Getter
    @Setter
    private volatile List<String> keys = null;
    @Getter
    @Setter
    private volatile Boolean enableAutoUpgrade = KimiConfig.KIMI_AUTO_UPGRADE;
    @Getter
    @Setter
    private volatile PromptTemplateDrive promptTemplateDrive;
    @Getter
    @Setter
    private volatile Function<KimiError<KimiChatRequest>, Boolean> chatErrorResolve = null;
    @Getter
    @Setter
    private volatile Function<KimiError<KimiFileMeta>, Boolean> fileErrorResolve = null;
    @Getter
    @Setter
    private volatile Function<KimiError<RequestBody>, Boolean> queryErrorResolve = null;
    @Getter
    @Setter
    private volatile Function<KimiError<KimiCacheUpdateRequest>, Boolean> cacheUpdateErrorResolve = null;
    @Getter
    @Setter
    private volatile Function<KimiError<KimiCacheRequest>, Boolean> cacheRequestErrorResolve = null;
    @Getter
    @Setter
    private volatile Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap = null;
    @Getter
    @Setter
    private volatile Map<String,String> cacheHeaders = null;

    private volatile SessionPackageTheadLocalAdaptor<ArrayList<CommonAIMessage>> historyLocalMap;
    private volatile SessionPackageTheadLocalAdaptor<String> jsonTemplate;
    private volatile SessionPackageTheadLocalAdaptor<KimiChatRequest> chatRequest;

    @Getter
    @Setter
    private volatile Boolean multi=KimiConfig.KIMI_MULTI;

    public KimiAIService() {
        promptTemplateDrive = new PromptTemplateDrive("我是人工智能AI-KIMI，请你对我进行提问，我会给你最准确的回答。");
        this.chatRequest = new SessionPackageTheadLocalAdaptor<KimiChatRequest>(multi);
        this.jsonTemplate = new SessionPackageTheadLocalAdaptor<String>(multi);
        this.historyLocalMap = new SessionPackageTheadLocalAdaptor<ArrayList<CommonAIMessage>>(multi);
        this.chatRequest.set(new KimiChatRequest<>());
        this.jsonTemplate.set("");
        this.historyLocalMap.set(new ArrayList<>());
    }
    public KimiAIService(PromptTemplateDrive promptTemplateDrive) {
        this.chatRequest = new SessionPackageTheadLocalAdaptor<KimiChatRequest>(multi);
        this.jsonTemplate = new SessionPackageTheadLocalAdaptor<String>(multi);
        this.historyLocalMap = new SessionPackageTheadLocalAdaptor<ArrayList<CommonAIMessage>>(multi);
        this.chatRequest.set(new KimiChatRequest<>());
        this.jsonTemplate.set("");
        this.historyLocalMap.set(new ArrayList<>());
        this.promptTemplateDrive = promptTemplateDrive;
        this.getHistory().add(promptTemplateDrive.generateTemplate());
    }
    public KimiAIService setHistory(ArrayList<CommonAIMessage> historyLocalMap) {
        this.historyLocalMap.set(historyLocalMap);
        return this;
    }
    public KimiAIService setJsonTemplate(SessionPackageTheadLocalAdaptor<String> jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
        return this;
    }
    public KimiAIService setChatRequest(SessionPackageTheadLocalAdaptor<KimiChatRequest> chatRequest) {
        this.chatRequest = chatRequest;
        return this;
    }
    public KimiAIService setChatRequest(KimiChatRequest chatRequest) {
        this.chatRequest.set(chatRequest);
        return this;
    }
    @Override
    public KimiAIService setProxy(String url, int port) {
        proxy = new Proxy(java.net.Proxy.Type.HTTP,new java.net.InetSocketAddress(url,port));
        return this;
    }
    @Override
    public KimiAIService auth(List<String> keyList) {
        this.keys = keyList;
        return this;
    }
    public PromptTemplateDrive getPromptTemplateDrive() {
        return promptTemplateDrive;
    }
    public List<CommonAIMessage> getHistory() {
        return historyLocalMap.get();
    }
    public String getJsonTemplate() {
        return jsonTemplate.get();
    }
    public SessionPackageTheadLocalAdaptor<KimiChatRequest> getChatRequest() {
        return chatRequest;
    }
    public KimiAIService openContextCacheMode(String cacheId,Integer dryRun,Integer resetTTL){
        KimiAIService result = new KimiAIService();
        BeanUtil.copyProperties(this,result);
        HashMap<String, String> cacheMap = new HashMap<>();
        cacheMap.put(KimiHeaders.Msh_Context_Cache_Id,cacheId);
        cacheMap.put(KimiHeaders.Msh_Context_Cache_Token_Saved, String.valueOf(dryRun));
        cacheMap.put(KimiHeaders.Msh_Context_Cache_Token_Exp, String.valueOf(resetTTL));
        result.setCacheHeaders(cacheHeaders);
        return result;
    }
    public KimiAIService closeContextCacheMode(){
        KimiAIService result = new KimiAIService();
        BeanUtil.copyProperties(this,result);
        result.setCacheHeaders(null);
        return result;
    }
    public KimiAIService useCacheTag(String tagName,Integer dryRun,Integer resetTTl){
        KimiAIService kimiAIService = new KimiAIService();
        BeanUtil.copyProperties(this, kimiAIService);
        kimiAIService.getHistory().add(0,new CommonAIMessage("cache","tag="+tagName+";dryrun="+dryRun+";reset_ttl="+resetTTl));
        return kimiAIService;
    }
    public KimiAIService unUseCacheTag(){
        KimiAIService kimiAIService = new KimiAIService();
        BeanUtil.copyProperties(this, kimiAIService);
        kimiAIService.getHistory().removeIf(v->"cache".equals(v.getRole()));
        return kimiAIService;
    }
    @Override
    public KimiAIService setPromptTemplateDrive(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = promptTemplateDrive;
        return this;
    }
    @Override
    public KimiAIService setJsonTemplate(String jsonTemplate) {
        this.jsonTemplate.set(jsonTemplate);
        return this;
    }
    @Override
    public KimiAIService setJsonTemplate(Object dao) {
        this.jsonTemplate.set(new Gson().toJson(dao));
        return this;
    }
    @Override
    public KimiAIService claerHistory() {
        this.getHistory().clear();
        return this;
    }
    @Override
    public KimiAIService reload(PromptTemplateDrive drive) {
        this.claerHistory();
        this.promptTemplateDrive=drive;
        this.getHistory().add(promptTemplateDrive.generateTemplate());
        return this;
    }


    private EventSource askStreamAdapter(String msg, Map<String,Function<Map<String,String>,String>> kimiFunctionDriverMap, Consumer<String> kimiAiResponseDetail, Function<KimiError<KimiChatRequest>,Boolean> errorResolve, CountDownLatch eventLatch){
        List<CommonAIMessage> messages;
        KimiChatRequest request = Optional.ofNullable(kimiFunctionDriverMap)
                .map(v->this.getChatRequest().get())
                .orElseGet(() -> {
                    KimiChatRequest KimiChatRequest = new KimiChatRequest();
                    BeanUtil.copyProperties(this.getChatRequest().get(),KimiChatRequest);
                    KimiChatRequest.setTools(null);
                    return KimiChatRequest;
                });
        request.setResponse_format(KimiResponseFormatEnum.TEXT);
        if (!request.getStream()){
            log.error("[Kimi-AI-AskSSE]:ask调用请使用 ask 系列方法，采用askStream系列方法固定走向为stream");
            request.setStream(true);
        }

        messages = this.getHistory();
        CommonAIMessage commonAIMessage = this.promptTemplateDrive.generateTemplate();
        if (!messages.contains(commonAIMessage)){
            messages.add(commonAIMessage);
        }
        if (null!=msg){
            messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
        }
        request.setMessages(messages);

        Kimi kimi = KimiBuilder.create().chat().completions().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade);
        Callable callBack = () -> askStreamAdapter(null, kimiFunctionDriverMap, kimiAiResponseDetail, errorResolve, eventLatch);
        KimiEventSourceLinsener kimiEventSourceLinsener = new KimiEventSourceLinsener(kimiAiResponseDetail,
                callBack,kimiFunctionDriverMap,this.getHistory(), eventLatch);

        // 如果开启缓存，那么放入header
        EventSource eventSource = Optional
                .ofNullable(this.getCacheHeaders())
                .map(v -> kimi.STREAM(request, v, kimiEventSourceLinsener,errorResolve))
                .orElse(kimi.STREAM(request, kimiEventSourceLinsener,errorResolve));
        try {
            eventLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return eventSource;
    }
    public EventSource askStream(String msg,Consumer<String> kimiAiResponseDetail, Function<KimiError<KimiChatRequest>,Boolean> errorResolve){
        return askStreamAdapter(msg,null,kimiAiResponseDetail,errorResolve, new CountDownLatch(1));
    }
    public EventSource askStream(String msg,Consumer<String> kimiAiResponseDetail){
        return askStream(msg,kimiAiResponseDetail,chatErrorResolve);
    }
    //    public EventSource askStreamInNet(String msg, Map<String,Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<String> kimiAiResponseDetail, Function<KimiError,Boolean> errorResolve){
//        return askStreamAdapter(msg,kimiFunctionDriverMap,kimiAiResponseDetail,errorResolve, new CountDownLatch(1));
//    }
//    public EventSource askStreamInNet(String msg, Map<String,Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<String> kimiAiResponseDetail){
//        return askStreamInNet(msg,kimiFunctionDriverMap,kimiAiResponseDetail,null);
//    }
//    public EventSource askStreamInNet(String msg,Consumer<String> kimiAiResponseDetail){
//        return askStreamInNet(msg,kimiFunctionDriverMap,kimiAiResponseDetail,null);
//    }
//    public EventSource askStreamInNet(String msg,Consumer<String> kimiAiResponseDetail, Function<KimiError,Boolean> errorResolve){
//        return askStreamInNet(msg,kimiFunctionDriverMap,kimiAiResponseDetail,errorResolve);
//    }
    private String askAdapter(String msg, boolean usingHistory, boolean returnJson, Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap, Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        List<CommonAIMessage> messages = null;
        KimiChatRequest request = Optional.ofNullable(kimiFunctionDriverMap)
                .map(v->this.getChatRequest().get())
                .orElseGet(() -> {
                    KimiChatRequest KimiChatRequest = new KimiChatRequest();
                    BeanUtil.copyProperties(this.getChatRequest().get(),KimiChatRequest);
                    KimiChatRequest.setTools(null);
                    return KimiChatRequest;
                });
        if (request.getStream()){
            log.error("[Kimi-AI-Ask]:SSE请调用 askStram 系列方法，采用ask系列方法固定走向为!stream");
            request.setStream(false);
        }
        if (returnJson && null != msg) {
            request.setResponse_format(KimiResponseFormatEnum.JSON);
            msg += "请你按照以下Json格式回复我：\n" +this.getJsonTemplate();
        }
        else {
            request.setResponse_format(KimiResponseFormatEnum.TEXT);
        }
        // 如果不使用历史记录
        // tips: 使用历史记录的条件：开启历史记录，开启上下文缓存使用，如果有ToolCalls，或者msg为空，那么强制使用历史记录
        if (!usingHistory && null != msg) {
            messages = new ArrayList<>();
            messages.addAll(Arrays.asList(promptTemplateDrive.generateTemplate(),new CommonAIMessage(CommonAIRoleEnum.USER, msg)));
        }
        else{
            messages = this.getHistory();
            CommonAIMessage commonAIMessage = this.promptTemplateDrive.generateTemplate();
            if (!messages.contains(commonAIMessage)){
                messages.add(commonAIMessage);
            }
            if (null!=msg){
                CommonAIMessage lastRequest = messages.get(messages.size() - 1);
                if (lastRequest instanceof KimiAiMergeMessage){
                    KimiAiMergeMessage mergeMessage = (KimiAiMergeMessage) lastRequest;
                    List<ToolCalls> toolCalls = mergeMessage.getTool_calls();
                    if (!CollectionUtils.isEmpty(toolCalls)){
                        messages.remove(messages.size()-1);
                        messages.remove(messages.size()-1);
                    }
                }
                messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
            }
        }
        List orignMsg = request.getMessages();
        request.setMessages(messages);
        Kimi kimi = KimiBuilder.create().chat().completions().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade);
        // 如果开启缓存，那么放入header
        KimiChatResponse post = Optional
                .ofNullable(this.getCacheHeaders())
                .map(v->kimi.REQUEST(Method.POST, request,KimiChatResponse.class,v,errorResolve))
                .orElse(kimi.REQUEST(Method.POST, request,KimiChatResponse.class,errorResolve));
        KimiChatResponse.Choice choice = post.getChoices().get(0);
        String finishReason = choice.getFinish_reason();
        Boolean isInit = false;
        // 如果返回的结果不是STOP，那么递归继续
        if (KimiMessageFinishResonConstants.TOOL_CALLS.equals(finishReason)){

            String json = kimi.getResponseResultTempStorage().get();
            KimiChatResponse body = kimi.anlayseResponse(json,new TypeToken<KimiChatResponse>() {
            });
            if (null == kimiFunctionDriverMap) {
                throw new KaToolException(ErrorCode.OPER_ERROR,"没有设置工具驱动，请使用setKimiFunctionDriverMap方法设置");
            }
            KimiChatResponse.Choice backChoice = body.getChoices().get(0);
            KimiAiMergeMessage message = backChoice.getMessage();
            this.getHistory().add(message);
            List<ToolCalls> toolCalls = message.getTool_calls();
            toolCalls.forEach(toolCall->{
                ToolCallsFuntion function = toolCall.getFunction();
                String functionName = function.getName();
                String arguments = function.getArguments();
                String result = kimiFunctionDriverMap.get(functionName).apply(new Gson().fromJson(arguments,Map.class));
                KimiAiToolCallsMessage reqMsg = new KimiAiToolCallsMessage();
                reqMsg.setTool_call_id(toolCall.getId())
                        .setName(functionName)
                        .setContent(result)
                        .setRole(CommonAIRoleEnum.TOOL.getRole());
                this.getHistory().add(reqMsg);
            });
            return askAdapter(null,usingHistory,returnJson,kimiFunctionDriverMap,errorResolve);
        }
        CommonAIMessage message = choice.getMessage();
        if (usingHistory){
            // 这个时候需要将之前放入的TOOl_CALLS的message删除
//            this.getHistory().removeIf(v->CommonAIRoleEnum.TOOL.equals(v.getRole()));
            this.getHistory().add(message);
        }
        else{
            request.setMessages(orignMsg);
        }
        return message.getContent();
    }
    public String askWithContextInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,true,false,kimiFunctionDriverMap,errorResolve);
    }
    public String askBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,errorResolve);
    }
    public String askWithContextBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,errorResolve);
    }
    public Object askBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,errorResolve), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,errorResolve), type);
    }
    public String askWithContextInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg, true, false, kimiFunctionDriverMap, chatErrorResolve);
    }
    public String askBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,chatErrorResolve);
    }
    public String askWithContextBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,chatErrorResolve);
    }
    public Object askBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,chatErrorResolve), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,chatErrorResolve), type);
    }
    public String askWithContextInNet(String msg,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,true,false,kimiFunctionDriverMap,errorResolve);
    }
    public String askBackJsonInNet(String msg,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,errorResolve);
    }
    public String askWithContextBackJsonInNet(String msg,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,errorResolve);
    }
    public Object askBackDaoInNet(String msg, Type type,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,errorResolve), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,errorResolve), type);
    }
    public String askWithContextInNet(String msg) {
        return askWithContextInNet(msg,kimiFunctionDriverMap);
    }
    public String askBackJsonInNet(String msg) {
        return askBackJsonInNet(msg,kimiFunctionDriverMap);
    }
    public String askWithContextBackJsonInNet(String msg) {
        return askWithContextBackJsonInNet(msg,kimiFunctionDriverMap);
    }
    public Object askBackDaoInNet(String msg, Type type) {
        return askBackDaoInNet(msg, type,kimiFunctionDriverMap);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type) {
        return askWithContextBackDaoInNet(msg, type,kimiFunctionDriverMap);
    }
    @Override
    public String ask(String msg, Function<KimiError<KimiChatRequest>, Boolean> errorResolve) {
        return askAdapter(msg,false,false,null,errorResolve);
    }
    @Override
    public String askWithContext(String msg, Function<KimiError<KimiChatRequest>, Boolean> errorResolve) {
        return askWithContextInNet(msg,null,errorResolve);
    }
    @Override
    public String askBackJson(String msg, Function<KimiError<KimiChatRequest>, Boolean> errorResolve) {
        return askBackJsonInNet(msg,null,errorResolve);
    }
    @Override
    public String askWithContextBackJson(String msg,Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askWithContextBackJsonInNet(msg,null,errorResolve);
    }
    @Override
    public Object askBackDao(String msg, Type type, Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return askBackDaoInNet(msg,type,null,errorResolve);
    }
    @Override
    public Object askWithContextBackDao(String msg, Type type) {
        return  askWithContextBackDao(msg,type,null);
    }
    volatile AiServiceHttpUtil httpUtil = new AiServiceHttpUtil();
    @Override
    public Object askWithContextBackDao(String msg, Type type, Function<KimiError<KimiChatRequest>,Boolean> errorResolve) {
        return  askWithContextBackDaoInNet(msg,type,null,errorResolve);
    }
    @Override
    public String uploadFile(File file, Function<KimiError<KimiFileMeta>, Boolean> errorResolve) {
        KimiFileMeta upload = KimiBuilder.create().files().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).upload(file,errorResolve);
        return upload.getId();
    }
    @Override
    public List<String> uploadFile(List<File> files, Function<KimiError<KimiFileMeta>, Boolean> errorResolve) {
        List<KimiFileMeta> kimiFileMetas = KimiBuilder.create().files().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).uploadFiles(files,errorResolve);
        return  kimiFileMetas.stream().map(KimiFileMeta::getId).collect(Collectors.toList());
    }
    @Override
    public String uploadFile(String filePath, Function<KimiError<KimiFileMeta>, Boolean> errorResolve) {
        KimiFileMeta upload = KimiBuilder.create().files().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).upload(HttpUtil.downloadFileFromUrl(filePath,System.getProperty("user.dir")),errorResolve);
        return upload.getId();
    }
    @Override
    public List<String> uploadFileOfUrls(List<String> filePaths, Function<KimiError<KimiFileMeta>, Boolean> errorResolve) {
        List<File> collect = filePaths
                .parallelStream().map(v -> HttpUtil.downloadFileFromUrl(v, System.getProperty("user.dir"))).collect(Collectors.toList());
        return uploadFile(collect,errorResolve);
    }
    @Override
    public KimiBaseResponse<List<KimiFileMeta>> listOfFile(Function<KimiError<KimiFileMeta>, Boolean> errorResolve){
        return KimiBuilder.create().files().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(new TypeToken<KimiBaseResponse<List<KimiFileMeta>>>(){}.getType(),errorResolve);
    }
    @Override
    public KimiFileMeta getFileMeta(String fileId, Function<KimiError<KimiFileMeta>, Boolean> errorResolve){
        return KimiBuilder.create().files().id(fileId).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(KimiFileMeta.class,errorResolve);
    }
    @Override
    public KimiDefaultDeleteResponse deleteFile(String fileId, Function<KimiError<KimiFileMeta>, Boolean> errorResolve){
        return KimiBuilder.create().files().id(fileId).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).DELETE(KimiDefaultDeleteResponse.class,errorResolve);
    }
    @Override
    public KimiFileContentResponse getFileContent(String fileId, Function<KimiError<KimiFileMeta>, Boolean> errorResolve){
        return KimiBuilder.create().files().id(fileId).content().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(KimiFileContentResponse.class,errorResolve);
    }
    @Override
    public Long countToken(){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).POST(this.chatRequest.get(), KimiOtherResponse.class,null).getData().getTotal_tokens();
    }
    @Override
    public Long countToken(Function<KimiError<KimiOtherResponse.KimiOtherResponseData>, Boolean> errorResolve){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).POST(this.chatRequest.get(), KimiOtherResponse.class,errorResolve).getData().getTotal_tokens();
    }
    @Override
    public Long countToken(List<CommonAIMessage> chatRequest, Function<KimiError<KimiChatRequest>, Boolean> errorResolve){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade)
                .POST(this.getChatRequest().get().setMessages(chatRequest),KimiOtherResponse.class,errorResolve).getData().getTotal_tokens();
    }
    @Override
    public Long countToken(List<CommonAIMessage> chatRequest){
        return countToken(chatRequest,null);
    }
    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(Function<KimiError<KimiOtherResponse.KimiOtherResponseData>, Boolean> errorResolve){
        KimiOtherResponse get = KimiBuilder.create().users().me().balance().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(KimiOtherResponse.class, errorResolve);
        return get.getData();
    }
    @Override
    public String ask(String msg) {
        return ask(msg,chatErrorResolve);
    }
    @Override
    public String askWithContext(String msg) {
        return askWithContext(msg,chatErrorResolve);
    }
    @Override
    public String askBackJson(String msg) {
        return askBackJson(msg,chatErrorResolve);
    }
    @Override
    public String askWithContextBackJson(String msg) {
        return askWithContextBackJson(msg,chatErrorResolve);
    }
    @Override
    public Object askBackDao(String msg, Type type) {
        return askBackDao(msg,type,chatErrorResolve);
    }
    @Override
    public String uploadFile(File file) {
        return uploadFile(file,fileErrorResolve);
    }
    @Override
    public List<String> uploadFile(List<File> files) {
        return uploadFile(files,fileErrorResolve);
    }
    @Override
    public String uploadFile(String filePath) {
        return uploadFile(filePath,fileErrorResolve);
    }
    @Override
    public List<String> uploadFileOfUrls(List<String> filePaths) {
        return uploadFileOfUrls(filePaths,fileErrorResolve);
    }
    @Override
    public KimiBaseResponse<List<KimiFileMeta>> listOfFile(){
        return listOfFile(fileErrorResolve);
    }
    @Override
    public KimiFileMeta getFileMeta(String fileId){
        return getFileMeta(fileId,fileErrorResolve);
    }
    @Override
    public KimiDefaultDeleteResponse deleteFile(String fileId){
        return deleteFile(fileId, fileErrorResolve);
    }
    @Override
    public KimiFileContentResponse getFileContent(String fileId){
        return getFileContent(fileId,fileErrorResolve);
    }
    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(){
        return queryMoney(null);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,Function<KimiError<KimiCacheRequest>,Boolean> errorResolve){
        return applayContextCache(cacheRequest,false,errorResolve);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,boolean isThrow,Function<KimiError<KimiCacheRequest>,Boolean> errorResolve){
        KimiCacheResponse kimiCacheResponse = KimiBuilder.create().caching().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).POST(cacheRequest, KimiCacheResponse.class,errorResolve);
        if (KimiCacheStatus.ERROR.equals(kimiCacheResponse.getStatus())){
            if (isThrow){
                throw new KaToolException(ErrorCode.OPER_ERROR, KimiGsonFactory.create().toJson(kimiCacheResponse.getError()));
            }
            else {
                log.warn("【Kimi-AI-Cache-Error】: Type: {}    Message: {}", kimiCacheResponse.getError().getType(),kimiCacheResponse.getError().getMessage());
            }
        }
        return kimiCacheResponse;
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata,Function<KimiError<RequestBody>,Boolean> errorResolve){
        KimiCacheBuilder caching = KimiBuilder.create().caching();
        limit = Optional.ofNullable(limit).orElse(20);
        isAsc = Optional.ofNullable(isAsc).orElse(false);
        caching.limit(limit).order(isAsc);
        if (StringUtils.isNotBlank(afterId)){
            caching.after(afterId);
        }
        if (StringUtils.isNotBlank(afterId)){
            caching.before(beforeId);
        }
        if (metadata != null){
            caching.metadata(metadata.getKey(), metadata.getValue());
        }
        return caching.build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(new TypeToken<KimiBaseResponse<List<KimiCacheQueryData>>>(){}.getType(),errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, Boolean isAsc,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCache(limit, isAsc, null, null, null,errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCache(limit, null,errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCache(null, null,errorResolve);
    }
    public KimiCacheResponse queryCache(String cacheId,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return KimiBuilder.create().caching().id(cacheId).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(KimiCacheResponse.class,errorResolve);
    }
    public KimiDefaultDeleteResponse deleteCache(String cacheId,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return KimiBuilder.create().caching().id(cacheId).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).DELETE(KimiDefaultDeleteResponse.class,errorResolve);
    }
    public KimiCacheResponse reloadCache(KimiCacheUpdateRequest cacheUpdateRequest,Function<KimiError<KimiCacheUpdateRequest>,Boolean> errorResolve){
        return KimiBuilder.create().caching().id(cacheUpdateRequest.getId()).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).PUT(cacheUpdateRequest.getMeta(),KimiCacheResponse.class,errorResolve);
    }
    public KimiCacheTagMeta createCacheTag(String cacheId, String tag,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return KimiBuilder.create().caching().refs().tags().build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).POST(new KimiCacheTagCreateRequest(tag, cacheId), KimiCacheTagMeta.class,errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata,Function<KimiError<RequestBody>,Boolean> errorResolve){
        KimiCacheRefsTagsBuilder tags = KimiBuilder.create().caching().refs().tags();
        limit = Optional.ofNullable(limit).orElse(20);
        isAsc = Optional.ofNullable(isAsc).orElse(false);
        tags.limit(limit).order(isAsc);
        if (StringUtils.isNotBlank(afterId)){
            tags.after(afterId);
        }
        if (StringUtils.isNotBlank(afterId)){
            tags.before(beforeId);
        }
        return tags.build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(new TypeToken<KimiBaseResponse<List<KimiCacheTagMeta>>>(){}.getType(),errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, Boolean isAsc,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCacheTag(limit, isAsc, null, null, null,errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCacheTag(limit, null,errorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Function<KimiError<RequestBody>,Boolean> errorResolve){
        return listOfContextCacheTag(null, null,errorResolve);
    }
    public KimiTagDeleteResponse deleteCacheTag(String tagName,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).DELETE(KimiTagDeleteResponse.class,errorResolve);
    }
    public KimiCacheTagMeta queryCacheTag(String tagName,Function<KimiError<RequestBody>,Boolean> errorResolve){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build(httpUtil,cacheHeaders).auth(keys).proxy(proxy).enableAutoUpgrade(enableAutoUpgrade).GET(KimiCacheTagMeta.class,errorResolve);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest){
        return applayContextCache(cacheRequest, cacheRequestErrorResolve);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,boolean isThrow){
        return applayContextCache(cacheRequest, isThrow, cacheRequestErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
        return listOfContextCache(limit, isAsc, afterId, beforeId, metadata, queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc){
        return listOfContextCache(limit, isAsc, queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit){
        return listOfContextCache(limit,queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(){
        return listOfContextCache(null, queryErrorResolve);
    }
    public KimiCacheResponse reloadCache(KimiCacheUpdateRequest cacheUpdateRequest){
        return reloadCache(cacheUpdateRequest, cacheUpdateErrorResolve);
    }
    public KimiCacheResponse queryCache(String cacheId){
        return queryCache(cacheId,queryErrorResolve);
    }
    public KimiDefaultDeleteResponse deleteCache(String cacheId){
        return deleteCache(cacheId,queryErrorResolve);
    }

    public KimiCacheTagMeta createCacheTag(String cacheId, String tag){
        return createCacheTag(cacheId, tag,queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
        return listOfContextCacheTag(limit, isAsc, afterId, beforeId, metadata,queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc){
        return listOfContextCacheTag(limit, isAsc,queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit){
        return listOfContextCacheTag(limit,queryErrorResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(){
        return listOfContextCacheTag(null, queryErrorResolve);
    }
    public KimiTagDeleteResponse deleteCacheTag(String tagName){
        return deleteCacheTag(tagName,queryErrorResolve);
    }
    public KimiCacheTagMeta queryCacheTag(String tagName){
        return queryCacheTag(tagName,queryErrorResolve);
    }
    public KimiAIService end(){
        this.chatRequest.remove();
        this.historyLocalMap.remove();
        this.jsonTemplate.remove();
        return this;
    }
}