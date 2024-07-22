package cn.katool.services.ai.server.kimi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.kimi.KimiCacheStatus;
import cn.katool.services.ai.constant.kimi.KimiHeaders;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.constant.kimi.KimiResponseFormatEnum;
import cn.katool.services.ai.model.builder.KimiBuilder;
import cn.katool.services.ai.model.builder.KimiCacheBuilder;
import cn.katool.services.ai.model.builder.KimiCacheRefsTagsBuilder;
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
import cn.katool.services.ai.model.dto.kimi.other.KimiOtherResponse;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class KimiAIService implements CommonAIService {


    public KimiAIService() {
        promptTemplateDrive = new PromptTemplateDrive("");
        history = new CopyOnWriteArrayList<>();
        this.chatRequest = new InheritableThreadLocal<KimiChatRequest>(){
            @Override
            protected KimiChatRequest initialValue() {
                return new KimiChatRequest();
            }
        };
    }

    PromptTemplateDrive promptTemplateDrive;

    List<CommonAIMessage> history;

    ThreadLocal<String> jsonTemplate = new InheritableThreadLocal<>();
    public KimiAIService setHistory(List<CommonAIMessage> history) {
        this.history = history;
        return this;
    }

    public KimiAIService setJsonTemplate(ThreadLocal<String> jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
        return this;
    }

    public KimiAIService setChatRequest(ThreadLocal<KimiChatRequest> chatRequest) {
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



    public PromptTemplateDrive getPromptTemplateDrive() {
        return promptTemplateDrive;
    }

    public List<CommonAIMessage> getHistory() {
        return history;
    }

    public String getJsonTemplate() {
        return jsonTemplate.get();
    }

    public ThreadLocal<KimiChatRequest> getChatRequest() {
        return chatRequest;
    }

    public Map<String, String> getCacheHeaders() {
        return cacheHeaders;
    }

    ThreadLocal<KimiChatRequest> chatRequest;

    Map<String,String> cacheHeaders = null;


    public KimiAIService(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = new PromptTemplateDrive();
        this.history = new CopyOnWriteArrayList<>();
        this.history.add(promptTemplateDrive.generateTemplate());
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
        List<CommonAIMessage> history = kimiAIService.getHistory();
        history.add(0,new CommonAIMessage("cache","tag="+tagName+";dryrun="+dryRun+";reset_ttl="+resetTTl));
        return kimiAIService;
    }
    public KimiAIService unUseCacheTag(){
        KimiAIService kimiAIService = new KimiAIService();
        BeanUtil.copyProperties(this, kimiAIService);
        List<CommonAIMessage> history = kimiAIService.getHistory();
        history.removeIf(v->"cache".equals(v.getRole()));
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
        this.history.clear();
        return this;
    }

    @Override
    public CommonAIService reload(PromptTemplateDrive drive) {
        this.claerHistory();
        this.promptTemplateDrive=drive;
        this.history.add(promptTemplateDrive.generateTemplate());
        return this;
    }

    private String askAdapter(String msg,boolean usingHistory,boolean returnJson) {

        List<CommonAIMessage> messages;
        if (returnJson) {
            chatRequest.get().setResponse_format(KimiResponseFormatEnum.JSON);
            msg += "请你按照以下Json格式回复我：\n" +this.getJsonTemplate();
        }
        else {
            chatRequest.get().setResponse_format(KimiResponseFormatEnum.TEXT);
        }
        if (!usingHistory) {
            messages = new CopyOnWriteArrayList<>();
            messages.addAll(Arrays.asList(promptTemplateDrive.generateTemplate(),new CommonAIMessage(CommonAIRoleEnum.USER, msg)));
        }
        else{
            messages = history;
            messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
        }
        chatRequest.get().setMessages(messages);
        KimiChatResponse post = Optional
                .ofNullable(this.getCacheHeaders())
                .map(v->KimiBuilder.create().chat().completions().build().REQUEST(Method.POST,chatRequest.get(),KimiChatResponse.class))
                .orElse(KimiBuilder.create().chat().completions().build().REQUEST(Method.POST,chatRequest.get(),KimiChatResponse.class,this.cacheHeaders));
        CommonAIMessage message = post.getChoices().get(0).getMessage();
        if (usingHistory){
            this.history.add(message);
        }
        return message.getContent();
    }
    @Override
    public String ask(String msg) {
        return askAdapter(msg,false,false);
    }

    @Override
    public String askWithContext(String msg) {
        return askAdapter(msg,true,false);
    }

    @Override
    public String askBackJson(String msg) {
        return askAdapter(msg,false,true);
    }

    @Override
    public String askWithContextBackJson(String msg) {
        return askAdapter(msg,true,true);
    }

    @Override
    public Object askBackDao(String msg, Type type) {
        return new Gson().fromJson(askBackJson(msg), type);
    }

    @Override
    public Object askWithContextBackDao(String msg, Type type) {
        return  new Gson().fromJson(askWithContextBackJson(msg), type);
    }


    @Override
    public String uploadFile(File file) {
        KimiFileMeta upload = KimiBuilder.create().files().build().upload(file);
        return upload.getId();
    }

    @Override
    public List<String> uploadFile(List<File> files) {
        List<KimiFileMeta> kimiFileMetas = KimiBuilder.create().files().build().uploadFiles(files);
        return  kimiFileMetas.stream().map(KimiFileMeta::getId).collect(Collectors.toList());
    }

    @Override
    public String uploadFile(String filePath) {
        KimiFileMeta upload = KimiBuilder.create().files().build().upload(HttpUtil.downloadFileFromUrl(filePath,System.getProperty("user.dir")));
        return upload.getId();
    }

    @Override
    public List<String> uploadFileOfUrls(List<String> filePaths) {
        List<File> collect = filePaths
                .parallelStream().map(v -> HttpUtil.downloadFileFromUrl(v, System.getProperty("user.dir"))).collect(Collectors.toList());
        return uploadFile(collect);
    }

    @Override
    public KimiBaseResponse<List<KimiFileMeta>> listOfFile(){
        return KimiBuilder.create().files().build().GET(new TypeToken<KimiBaseResponse<List<KimiFileMeta>>>(){}.getType());
    }

    @Override
    public KimiFileMeta getFileMeta(String fileId){
        return KimiBuilder.create().files().id(fileId).build().GET(KimiFileMeta.class);
    }

    @Override
    public KimiDefaultDeleteResponse deleteFile(String fileId){
        return KimiBuilder.create().files().id(fileId).build().DELETE(KimiDefaultDeleteResponse.class);
    }

    @Override
    public KimiFileContentResponse getFileContent(String fileId){
        return KimiBuilder.create().files().id(fileId).content().build().GET(KimiFileContentResponse.class);
    }

    @Override
    public Long countToken(){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build().POST(this.chatRequest.get(), KimiOtherResponse.class).getData().getTotal_tokens();
    }

    @Override
    public Long countToken(List<CommonAIMessage> chatRequest){
        return KimiBuilder.create().tokenizers().estimateTokenCount().build()
                .POST(new KimiChatRequest()
                        .setModel(KimiModel.MOONSHOT_V1_32K)
                        .setMax_tokens(2000000).setMessages(chatRequest),KimiOtherResponse.class).getData().getTotal_tokens();
    }

    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(){
        return KimiBuilder.create().users().me().balance().build().GET(KimiOtherResponse.class).getData();
    }

    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest){
        return applayContextCache(cacheRequest,false);
    }
    public KimiCacheResponse applayContextCache(KimiCacheRequest cacheRequest,Boolean isThrow){
        KimiCacheResponse kimiCacheResponse = KimiBuilder.create().caching().build().POST(cacheRequest, KimiCacheResponse.class);
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
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, Boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
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
        return caching.build().GET(new TypeToken<KimiBaseResponse<List<KimiCacheQueryData>>>(){}.getType());
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit, Boolean isAsc){
        return listOfContextCache(limit, isAsc, null, null, null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(Integer limit){
        return listOfContextCache(limit, null);
    }
    public KimiBaseResponse<List<KimiCacheQueryData>> listOfContextCache(){
        return listOfContextCache(null, null);
    }

    public KimiCacheResponse queryCache(String cacheId){
        return KimiBuilder.create().caching().id(cacheId).build().GET(KimiCacheResponse.class);
    }
    public KimiDefaultDeleteResponse deleteCache(String cacheId){
        return KimiBuilder.create().caching().id(cacheId).build().DELETE(KimiDefaultDeleteResponse.class);
    }
    public KimiCacheResponse reloadCache(KimiCacheUpdateRequest cacheUpdateRequest){
        return KimiBuilder.create().caching().id(cacheUpdateRequest.getId()).build().PUT(cacheUpdateRequest.getMeta(),KimiCacheResponse.class);
    }

    public KimiCacheTagMeta createCacheTag(String cacheId, String tag){
        return KimiBuilder.create().caching().refs().tags().build().POST(new KimiCacheTagCreateRequest(tag, cacheId), KimiCacheTagMeta.class);
    }

    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, Boolean isAsc, String afterId, String beforeId, Pair<String, String> metadata){
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
        return tags.build().GET(new TypeToken<KimiBaseResponse<List<KimiCacheTagMeta>>>(){}.getType());
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit, Boolean isAsc){
        return listOfContextCacheTag(limit, isAsc, null, null, null);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(Integer limit){
        return listOfContextCacheTag(limit, null);
    }
    public KimiBaseResponse<List<KimiCacheTagMeta>> listOfContextCacheTag(){
        return listOfContextCacheTag(null, null);
    }
    public KimiTagDeleteResponse deleteCacheTag(String tagName){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build().DELETE(KimiTagDeleteResponse.class);
    }
    public KimiCacheTagMeta queryCacheTag(String tagName){
        return KimiBuilder.create().caching().refs().tags().tagName(tagName).build().GET(KimiCacheTagMeta.class);
    }

}
