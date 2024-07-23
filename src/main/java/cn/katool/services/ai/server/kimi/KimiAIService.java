package cn.katool.services.ai.server.kimi;

import ch.qos.logback.classic.util.CopyOnInheritThreadLocal;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.common.CopyOnTransmittableThreadLocal;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
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
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.services.ai.model.entity.kimi.KimiErrorMessage;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.hutool.core.lang.Pair;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class KimiAIService implements CommonAIService<KimiOtherResponse.KimiOtherResponseData,KimiErrorMessage> {

    volatile Proxy proxy = null;
    volatile String key = null;
    volatile PromptTemplateDrive promptTemplateDrive;

    volatile CopyOnTransmittableThreadLocal<ArrayList<CommonAIMessage>> historyLocalMap = new CopyOnTransmittableThreadLocal<ArrayList<CommonAIMessage>>() {
        @Override
        protected ArrayList<CommonAIMessage> initialValue() {
            return new ArrayList<>();
        }
    };;

    volatile CopyOnTransmittableThreadLocal<String> jsonTemplate = new CopyOnTransmittableThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }

    };
    volatile CopyOnTransmittableThreadLocal<KimiChatRequest> chatRequest;

    volatile Map<String,String> cacheHeaders = null;
    public KimiAIService() {
        promptTemplateDrive = new PromptTemplateDrive("我是人工智能AI-KIMI，请你对我进行提问，我会给你最准确的回答。");
        this.chatRequest = new CopyOnTransmittableThreadLocal<KimiChatRequest>(){

            @Override
            protected KimiChatRequest initialValue() {
                return new KimiChatRequest();
            }
        };
    }
    public KimiAIService setHistory(ArrayList<CommonAIMessage> historyLocalMap) {
        this.historyLocalMap.set(historyLocalMap);
        return this;
    }

    public KimiAIService setJsonTemplate(CopyOnTransmittableThreadLocal<String> jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
        return this;
    }

    public KimiAIService setChatRequest(CopyOnTransmittableThreadLocal<KimiChatRequest> chatRequest) {
        this.chatRequest = chatRequest;
        return this;
    }

    public KimiAIService setCacheHeaders(Map<String, String> cacheHeaders) {
        this.cacheHeaders = cacheHeaders;
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
    public KimiAIService auth(String key) {
        this.key = key;
        return this;
    }

    public PromptTemplateDrive getPromptTemplateDrive() {
        return promptTemplateDrive;
    }

    public ArrayList<CommonAIMessage> getHistory() {
        return historyLocalMap.get();
    }

    public String getJsonTemplate() {
        return jsonTemplate.get();
    }

    public CopyOnTransmittableThreadLocal<KimiChatRequest> getChatRequest() {
        return chatRequest;
    }

    public Map<String, String> getCacheHeaders() {
        return cacheHeaders;
    }




    public KimiAIService(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = promptTemplateDrive;
        this.getHistory().add(promptTemplateDrive.generateTemplate());
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
    public CommonAIService setPromptTemplateDrive(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = promptTemplateDrive;
        return this;
    }

    @Override
    public CommonAIService setJsonTemplate(String jsonTemplate) {
        this.jsonTemplate.set(jsonTemplate);
        return this;
    }

    @Override
    public CommonAIService setJsonTemplate(Object dao) {
        this.jsonTemplate.set(new Gson().toJson(dao));
        return this;
    }

    @Override
    public CommonAIService claerHistory() {
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

    public Proxy getProxy() {
        return proxy;
    }

    public KimiAIService setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public String getKey() {
        return key;
    }

    public KimiAIService setKey(String key) {
        this.key = key;
        return this;
    }

    public Map<String, Function<Map<String, String>, String>> getKimiFunctionDriverMap() {
        return kimiFunctionDriverMap;
    }

    public KimiAIService setKimiFunctionDriverMap(Map<String, Function<Map<String, String>, String>> kimiFunctionDriverMap) {
        this.kimiFunctionDriverMap = kimiFunctionDriverMap;
        return this;
    }

    private String askAdapter(String msg, boolean usingHistory, boolean returnJson, Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap, Consumer<KimiErrorMessage> throwResolve) {

        ArrayList<CommonAIMessage> messages;
        KimiChatRequest request = Optional.ofNullable(kimiFunctionDriverMap)
                .map(v->this.getChatRequest().get())
                .orElseGet(() -> {
                    KimiChatRequest kimiChatRequest = new KimiChatRequest();
                    BeanUtil.copyProperties(this.getChatRequest().get(),kimiChatRequest);
                    kimiChatRequest.setTools(null);
                    return kimiChatRequest;
                });
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
                messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
            }
        }
        request.setMessages(messages);
        Kimi kimi = KimiBuilder.create().chat().completions().build().auth(key).proxy(proxy);
        // 如果开启缓存，那么放入header
        KimiChatResponse post = Optional
                .ofNullable(this.getCacheHeaders())
                .map(v->kimi.REQUEST(Method.POST, request,KimiChatResponse.class,this.cacheHeaders,throwResolve))
                .orElse(kimi.REQUEST(Method.POST, request,KimiChatResponse.class,throwResolve));
        KimiChatResponse.Choice choice = post.getChoices().get(0);
        String finishReason = choice.getFinish_reason();
        Boolean isInit = false;
        KimiChatResponse body = null;
        // 如果返回的结果不是STOP，那么递归继续
        if (KimiMessageFinishResonConstants.TOOL_CALLS.equals(finishReason)){
                String json = kimi.getResponseResultTempStorage().get();
                body = kimi.anlayseResponse(json,new TypeToken<KimiChatResponse>() {
                });
            KimiChatResponse.Choice backChoice = body.getChoices().get(0);
            KimiAiMergeMessage message = backChoice.getMessage();
            this.getHistory().add(message);
            List<ToolCalls> toolCalls = message.getTool_calls();
            if (null == kimiFunctionDriverMap) {
                throw new KaToolException(ErrorCode.OPER_ERROR,"没有设置工具驱动，请使用setKimiFunctionDriverMap方法设置");
            }
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
            return askAdapter(null,usingHistory,returnJson,kimiFunctionDriverMap,throwResolve);
        }
        CommonAIMessage message = choice.getMessage();
        if (usingHistory){
            // 这个时候需要将之前放入的TOOl_CALLS的message删除
            this.getHistory().removeIf(v->CommonAIRoleEnum.TOOL.equals(v.getRole()));
            this.getHistory().add(message);
        }
        return message.getContent();
    }



    public String askWithContextInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,true,false,kimiFunctionDriverMap,throwResolve);
    }
    public String askBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,throwResolve);
    }
    public String askWithContextBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,throwResolve);
    }
    public Object askBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<KimiErrorMessage> throwResolve) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,throwResolve), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap,Consumer<KimiErrorMessage> throwResolve) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,throwResolve), type);
    }
    public String askWithContextInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg,true,false,kimiFunctionDriverMap,null);
    }
    public String askBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,null);
    }
    public String askWithContextBackJsonInNet(String msg,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,null);
    }
    public Object askBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,null), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,null), type);
    }

    public String askWithContextInNet(String msg,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,true,false,kimiFunctionDriverMap,throwResolve);
    }
    public String askBackJsonInNet(String msg,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,false,true,kimiFunctionDriverMap,throwResolve);
    }
    public String askWithContextBackJsonInNet(String msg,Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,true,true,kimiFunctionDriverMap,throwResolve);
    }
    public Object askBackDaoInNet(String msg, Type type,Consumer<KimiErrorMessage> throwResolve) {
        return new Gson().fromJson(askBackJsonInNet(msg, kimiFunctionDriverMap,throwResolve), type);
    }
    public Object askWithContextBackDaoInNet(String msg, Type type,Consumer<KimiErrorMessage> throwResolve) {
        return new Gson().fromJson(askWithContextBackJsonInNet(msg, kimiFunctionDriverMap,throwResolve), type);
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
    public String ask(String msg, Consumer<KimiErrorMessage> throwResolve) {
        return askAdapter(msg,false,false,null,throwResolve);
    }
    @Override
    public String askWithContext(String msg, Consumer<KimiErrorMessage> throwResolve) {
        return askWithContextInNet(msg,null,throwResolve);
    }
    Map<String, Function<Map<String,String>,String>> kimiFunctionDriverMap = null;

    @Override
    public String askBackJson(String msg, Consumer<KimiErrorMessage> throwResolve) {
        return askBackJsonInNet(msg,null,throwResolve);
    }
    @Override
    public String askWithContextBackJson(String msg,Consumer<KimiErrorMessage> throwResolve) {
        return askWithContextBackJsonInNet(msg,null,throwResolve);
    }
    @Override
    public Object askBackDao(String msg, Type type, Consumer<KimiErrorMessage> throwResolve) {
        return askBackDaoInNet(msg,type,null,throwResolve);
    }
    @Override
    public Object askWithContextBackDao(String msg, Type type) {
        return  askWithContextBackDao(msg,type,null);
    }
    @Override
    public Object askWithContextBackDao(String msg, Type type, Consumer<KimiErrorMessage> throwResolve) {
        return  askWithContextBackDaoInNet(msg,type,null,throwResolve);
    }
    @Override
    public String uploadFile(File file,Consumer<KimiErrorMessage> throwResolve) {
        KimiFileMeta upload = KimiBuilder.create().files().build().auth(key).proxy(proxy).upload(file,throwResolve);
        return upload.getId();
    }

    @Override
    public List<String> uploadFile(List<File> files,Consumer<KimiErrorMessage> throwResolve) {
        List<KimiFileMeta> kimiFileMetas = KimiBuilder.create().files().build().auth(key).proxy(proxy).uploadFiles(files,throwResolve);
        return  kimiFileMetas.stream().map(KimiFileMeta::getId).collect(Collectors.toList());
    }

    @Override
    public String uploadFile(String filePath,Consumer<KimiErrorMessage> throwResolve) {
        KimiFileMeta upload = KimiBuilder.create().files().build().auth(key).proxy(proxy).upload(HttpUtil.downloadFileFromUrl(filePath,System.getProperty("user.dir")),throwResolve);
        return upload.getId();
    }
    @Override
    public List<String> uploadFileOfUrls(List<String> filePaths,Consumer<KimiErrorMessage> throwResolve) {
        List<File> collect = filePaths
                .parallelStream().map(v -> HttpUtil.downloadFileFromUrl(v, System.getProperty("user.dir"))).collect(Collectors.toList());
        return uploadFile(collect,throwResolve);
    }
    @Override
    public KimiBaseResponse<List<KimiFileMeta>> listOfFile(Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().files().build().auth(key).proxy(proxy).GET(new TypeToken<KimiBaseResponse<List<KimiFileMeta>>>(){}.getType(),throwResolve);
    }
    @Override
    public KimiFileMeta getFileMeta(String fileId,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().files().id(fileId).build().auth(key).proxy(proxy).GET(KimiFileMeta.class,throwResolve);
    }
    @Override
    public KimiDefaultDeleteResponse deleteFile(String fileId,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().files().id(fileId).build().auth(key).proxy(proxy).DELETE(KimiDefaultDeleteResponse.class,throwResolve);
    }
    @Override
    public KimiFileContentResponse getFileContent(String fileId,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().files().id(fileId).content().build().auth(key).proxy(proxy).GET(KimiFileContentResponse.class,throwResolve);
    }
    @Override
    public Long countToken(){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build().auth(key).proxy(proxy).POST(this.chatRequest.get(), KimiOtherResponse.class,null).getData().getTotal_tokens();
    }
    @Override
    public Long countToken(Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build().auth(key).proxy(proxy).POST(this.chatRequest.get(), KimiOtherResponse.class,throwResolve).getData().getTotal_tokens();
    }

    @Override
    public Long countToken(List<CommonAIMessage> chatRequest,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build().auth(key).proxy(proxy)
                .POST(new KimiChatRequest()
                        .setModel(KimiModel.MOONSHOT_V1_32K)
                        .setMax_tokens(2000000).setMessages(chatRequest),KimiOtherResponse.class,throwResolve).getData().getTotal_tokens();
    }
    @Override
    public Long countToken(List<CommonAIMessage> chatRequest){
        return countToken(chatRequest,null);
    }

    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().users().me().balance().build().auth(key).proxy(proxy).GET(KimiOtherResponse.class,throwResolve).getData();
    }

    @Override
    public String ask(String msg) {
        return ask(msg,null);
    }

    @Override
    public String askWithContext(String msg) {
        return askWithContext(msg,null);
    }

    @Override
    public String askBackJson(String msg) {
        return askBackJson(msg,null);
    }

    @Override
    public String askWithContextBackJson(String msg) {
        return askWithContextBackJson(msg);
    }

    @Override
    public Object askBackDao(String msg, Type type) {
        return askBackDao(msg,type,null);
    }



    @Override
    public String uploadFile(File file) {
        return uploadFile(file,null);
    }

    @Override
    public List<String> uploadFile(List<File> files) {
        return uploadFile(files,null);
    }

    @Override
    public String uploadFile(String filePath) {
        return uploadFile(filePath,null);
    }

    @Override
    public List<String> uploadFileOfUrls(List<String> filePaths) {
        return uploadFileOfUrls(filePaths,null);
    }

    @Override
    public KimiBaseResponse<List<KimiFileMeta>> listOfFile(){
        return listOfFile(null);
    }

    @Override
    public KimiFileMeta getFileMeta(String fileId){
        return getFileMeta(fileId,null);
    }

    @Override
    public KimiDefaultDeleteResponse deleteFile(String fileId){
        return deleteFile(fileId,null);
    }

    @Override
    public KimiFileContentResponse getFileContent(String fileId){
        return getFileContent(fileId,null);
    }


    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(){
        return queryMoney(null);
    }

    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,Consumer<KimiErrorMessage> throwResolve){
        return applayContextCache(cacheRequest,false,throwResolve);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,boolean isThrow,Consumer<KimiErrorMessage> throwResolve){
        KimiCacheResponse kimiCacheResponse = KimiBuilder.create().caching().build().auth(key).proxy(proxy).POST(cacheRequest, KimiCacheResponse.class,throwResolve);
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
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata,Consumer<KimiErrorMessage> throwResolve){
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
        return caching.build().auth(key).proxy(proxy).GET(new TypeToken<KimiBaseResponse<List<KimiCacheQueryData>>>(){}.getType(),throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, Boolean isAsc,Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCache(limit, isAsc, null, null, null,throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit,Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCache(limit, null,throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCache(null, null,throwResolve);
    }

    public KimiCacheResponse queryCache(String cacheId,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().id(cacheId).build().auth(key).proxy(proxy).GET(KimiCacheResponse.class,throwResolve);
    }
    public KimiDefaultDeleteResponse deleteCache(String cacheId,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().id(cacheId).build().auth(key).proxy(proxy).DELETE(KimiDefaultDeleteResponse.class,throwResolve);
    }
    public KimiCacheResponse reloadCache(KimiCacheUpdateRequest cacheUpdateRequest,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().id(cacheUpdateRequest.getId()).build().auth(key).proxy(proxy).PUT(cacheUpdateRequest.getMeta(),KimiCacheResponse.class,throwResolve);
    }

    public KimiCacheTagMeta createCacheTag(String cacheId, String tag,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().refs().tags().build().auth(key).proxy(proxy).POST(new KimiCacheTagCreateRequest(tag, cacheId), KimiCacheTagMeta.class,throwResolve);
    }

    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata,Consumer<KimiErrorMessage> throwResolve){
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
        return tags.build().auth(key).proxy(proxy).GET(new TypeToken<KimiBaseResponse<List<KimiCacheTagMeta>>>(){}.getType(),throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, Boolean isAsc,Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCacheTag(limit, isAsc, null, null, null,throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit,Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCacheTag(limit, null,throwResolve);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Consumer<KimiErrorMessage> throwResolve){
        return listOfContextCacheTag(null, null,throwResolve);
    }
    public KimiTagDeleteResponse deleteCacheTag(String tagName,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build().auth(key).proxy(proxy).DELETE(KimiTagDeleteResponse.class,throwResolve);
    }
    public KimiCacheTagMeta queryCacheTag(String tagName,Consumer<KimiErrorMessage> throwResolve){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build().auth(key).proxy(proxy).GET(KimiCacheTagMeta.class,throwResolve);
    }


    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest){
        return applayContextCache(cacheRequest, null);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,boolean isThrow){
        return applayContextCache(cacheRequest,isThrow,null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
        return listOfContextCache(limit, isAsc, afterId, beforeId, metadata, null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, boolean isAsc){
        return listOfContextCache(limit, isAsc, null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit){
        return listOfContextCache(limit,null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(){
        return listOfContextCache(null, null);
    }

    public KimiCacheResponse queryCache(String cacheId){
        return queryCache(cacheId,null);
    }
    public KimiDefaultDeleteResponse deleteCache(String cacheId){
        return deleteCache(cacheId,null);
    }
    public KimiCacheResponse reloadCache(KimiCacheUpdateRequest cacheUpdateRequest){
        return reloadCache(cacheUpdateRequest,null);
    }

    public KimiCacheTagMeta createCacheTag(String cacheId, String tag){
        return createCacheTag(cacheId, tag,null);
    }

    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
        return listOfContextCacheTag(limit, isAsc, afterId, beforeId, metadata,null);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, boolean isAsc){
        return listOfContextCacheTag(limit, isAsc,null);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit){
        return listOfContextCacheTag(limit,null);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(){
        return listOfContextCacheTag(null, null);
    }
    public KimiTagDeleteResponse deleteCacheTag(String tagName){
        return deleteCacheTag(tagName,null);
    }
    public KimiCacheTagMeta queryCacheTag(String tagName){
        return queryCacheTag(tagName,null);
    }

    public KimiAIService end(){
        this.chatRequest.remove();
        this.historyLocalMap.remove();
        this.jsonTemplate.remove();
        return this;
    }
}
