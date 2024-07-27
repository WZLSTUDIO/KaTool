package cn.katool.services.ai.model.entity.kimi;
import cn.hutool.http.*;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.config.ai.kimi.KimiProxyConfig;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.common.KimiAIServiceFactory;
import cn.katool.services.ai.common.KimiAiUtils;
import cn.katool.services.ai.common.KimiEventSourceLinsener;
import cn.katool.services.ai.constant.kimi.KimiBuilderEnum;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.model.builder.kimi.KimiBuilder;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatRequest;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatResponse;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiMergeMessage;
import cn.katool.services.ai.model.dto.kimi.message.KimiAiMessage;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import cn.katool.services.ai.model.entity.RequestBody;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import cn.katool.services.ai.server.kimi.KimiAIService;
import cn.katool.util.AiServiceHttpUtil;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Kimi{
    private volatile Map<String, String> cacheHeaders;
    volatile KimiBuilder kimiBuilder;
    volatile String key;
    volatile Proxy proxy;
    volatile AiServiceHttpUtil httpUtil;
    volatile TransmittableThreadLocal<String> contentType = new TransmittableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "application/json";
        }
    };
    volatile TransmittableThreadLocal<RequestBody> requesetResultTempStorage = new TransmittableThreadLocal<RequestBody>(){
        @Override
        protected RequestBody initialValue() {
            return null;
        }
    };
    volatile TransmittableThreadLocal<String> responseResultTempStorage = new TransmittableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "{}";
        }
    };
    public Kimi(KimiBuilder kimiBuilder, String key, TransmittableThreadLocal<String> contentType, AiServiceHttpUtil httpUtil,Map<String, String> cacheHeaders) {
        this.kimiBuilder = kimiBuilder;
        this.key = key;
        this.contentType = contentType;
        this.httpUtil = httpUtil;
        this.cacheHeaders = cacheHeaders;
    }
    public Kimi proxy(Proxy proxy){
        if (null == proxy){
            return this;
        }
        this.proxy = proxy;
        return this;
    }
    private void validUnLegal(KimiBuilderEnum kimiBuilderEnum) {
        if (this.getKimiBuilder().getMaster().equals(kimiBuilderEnum)){
            throw new KaToolException(ErrorCode.OPER_ERROR,Thread.currentThread().getStackTrace()[2].getMethodName()+" method is not supported in file mode");
        }
    }
    private void validLegal(KimiBuilderEnum kimiBuilderEnum) {
        if (!this.getKimiBuilder().getMaster().equals(kimiBuilderEnum)){
            throw new KaToolException(ErrorCode.OPER_ERROR,Thread.currentThread().getStackTrace()[2].getMethodName()+" method is not supported in file mode");
        }
    }
    public Kimi auth(String key){
        if (StringUtils.isBlank(key)){
            return this;
        }
        this.key = key;
        return this;
    }
    public Kimi contentType(String type){
        this.contentType.set(type);
        return this;
    }
    private <T> Request.Builder getRequest(Method method, T body){
        return getRequest(method.name(),body);
    }

    private <T> Request.Builder getRequest(String method, T body){
        if (StringUtils.isBlank(key)){
            throw new KaToolException(ErrorCode.OPER_ERROR,"kimi-key is null");
        }
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(kimiBuilder.getUrl());
        if (KimiProxyConfig.ENABLE){
            httpUtil.createHttpClient(new Proxy(Proxy.Type.HTTP,InetSocketAddress.createUnresolved(KimiProxyConfig.HOST,KimiProxyConfig.PORT)));
        }
        else if (this.proxy !=null ){
            httpUtil.createHttpClient(this.proxy);
        }
        else {
            httpUtil.createHttpClient();
        }
        String requestJson = KimiGsonFactory.create().toJson(body);
        Type type = new TypeToken<T>() {
        }.getType();
        this.requesetResultTempStorage.set((RequestBody) body);
        requestBuilder.method(method,
                okhttp3.RequestBody.create(MediaType.parse(this.contentType.get()+"; charset=utf-8"),
                        requestJson));
        requestBuilder.addHeader("Authorization", "Bearer " + key);
        return requestBuilder;
    }

    @SneakyThrows
    private <T extends RequestBody,R> R detailRequest(Request request, Type responseClass, Function<KimiError<T>,Boolean> errorResolve){
        Response httpResponse = httpUtil.getClient().newCall(request)
                .execute();
        String resJson = httpResponse.body().string();
        boolean contains = resJson.contains("\"finish_reason\":\"length\"");
        if (!httpResponse.isSuccessful()|| contains){
            KimiError<T> kimiError =
                    httpResponse.isSuccessful()?
                            new KimiError<T>().setError(new KimiErrorMessage().setMessage("token is too large, back json is [" + resJson + "]").setCode(ErrorCode.PARAMS_ERROR.getCode()))
                            : KimiGsonFactory.create().fromJson(resJson, new TypeToken<KimiError<T>>(){}.getType());

            T requestBody = (T) this.requesetResultTempStorage.get();
            kimiError.setRequestBody(requestBody);
            if (contains || kimiError.getError().getMessage().contains("Your request exceeded model token limit")){
                KimiChatRequest kimiChatRequest = (KimiChatRequest) requestBody;
                String model = kimiChatRequest.getModel();
                Long maxToken = KimiAiUtils.getMaxToken(model);
                Long aLong = KimiAIServiceFactory.createKimiAiService(KimiModel.MOONSHOT_V1_8K,null,null).setChatRequest(kimiChatRequest).countToken(kimiChatRequest.getMessages());
                if (maxToken < aLong) {
                    String nextModel = KimiAiUtils.getNextModel(model);
                    Long nextMaxToken = KimiAiUtils.getMaxToken(nextModel);
                    if (nextMaxToken < aLong) {
                        nextModel = KimiAiUtils.getNextModel(nextModel);
                        nextMaxToken = KimiAiUtils.getMaxToken(nextModel);
                    }
                    log.warn("KimiAiService检测到回复断言，尝试扩容并自动请求下文中... request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",aLong,kimiChatRequest.getMax_tokens(),maxToken,nextMaxToken);
                    kimiChatRequest.setModel(nextModel);
                    kimiChatRequest.setMax_tokens(nextMaxToken-aLong);
                }
                else{
                    log.warn("KimiAiService检测到回复断言，尝试扩容并自动请求下文中... request-token:{} ; setting-maxtoken:{} ; mode-maxtoken:{} ; target-token:{}",aLong,kimiChatRequest.getMax_tokens(),maxToken,maxToken-aLong);
                    kimiChatRequest.setMax_tokens(maxToken-aLong);
                }
//                List<CommonAIMessage> messages = kimiChatRequest.getMessages();
//                KimiChatResponse response = KimiGsonFactory.create().fromJson(resJson, responseClass);
//                KimiAiMergeMessage message = response.getChoices().get(0).getMessage();
//                message.setPartial(true);
//                String content = message.getContent();
//                if (!content.startsWith("result with:") && !"result with:".equals(messages.get(messages.size()-1).getContent())){
//                    content = "result with:" + content;
//                    message.setContent(content);
//                    messages.add(message);
//                }
                Request newRequest = this.getRequest(request.method(), kimiChatRequest).build();
                return detailRequest(newRequest, responseClass, errorResolve);
            }
            if (errorResolve != null) {
                log.error("kimi request error:{}", kimiError.getError().getMessage());
                if (errorResolve.apply(kimiError)) {
                    T reqBody = kimiError.getRequestBody();
                    Request newRequest = this.getRequest(request.method(), reqBody).build();
                    return detailRequest(newRequest, responseClass, errorResolve);
                }
            }
            else {
                throw new KaToolException(ErrorCode.OPER_ERROR,kimiError.getError().getMessage());
            };
        }
        responseResultTempStorage.set(resJson);
        return KimiGsonFactory.create().fromJson(resJson, responseClass);
    }
    public <T,E extends RequestBody,R> R POST(T request,Type responseClass,Function<KimiError<E>,Boolean> errorResolve){
        validUnLegal(KimiBuilderEnum.FILES);
        Request post = this.getRequest(Method.POST,request).build();
        return detailRequest(post,responseClass,errorResolve);
    }
    public <T,E extends RequestBody,R> R POST(T request,Class<R> responseClass,Function<KimiError<E>,Boolean> errorResolve){
        return POST(request,(Type)responseClass,errorResolve);
    }

    public <T extends RequestBody,R> R GET(Type resposneClass,Function<KimiError<T>,Boolean> errorResolve){
        Request request = this.getRequest(Method.GET,null).build();
        return detailRequest(request, resposneClass,errorResolve);
    }
    public <T extends RequestBody,R> R GET(Class<R> resposneClass,Function<KimiError<T>,Boolean> errorResolve){
        return GET((Type)resposneClass,errorResolve);
    }
    public <T extends RequestBody,R> R DELETE(Type responseClass,Function<KimiError<T>,Boolean> errorResolve){
        Request request = this.getRequest(Method.DELETE,null).build();
        return detailRequest(request, responseClass,errorResolve);
    }
    public <T extends RequestBody,R> R DELETE(Class<R> responseClass,Function<KimiError<T>,Boolean> errorResolve){
        return DELETE((Type)responseClass,errorResolve);
    }
    public <T,E extends RequestBody,R> R PUT(T request,Type responseClass,Function<KimiError<E>,Boolean> errorResolve){
        Request put = this.getRequest(Method.PUT,request).build();
        return detailRequest(put,responseClass,errorResolve);
    }

    public <T,E extends RequestBody,R> R PUT(T request,Class<R> responseClass,Function<KimiError<E>,Boolean> errorResolve){
        return PUT(request,(Type)responseClass,errorResolve);
    }
    public <T extends RequestBody,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass,Map<String,String>headers,Function<KimiError<T>,Boolean> errorResolve){
        AtomicReference<Request.Builder> req = new AtomicReference<>(this.getRequest(httpMethod, null));
        Optional.ofNullable(request).ifPresent(body->{
            String reqJson = KimiGsonFactory.create().toJson(body);
            log.debug("KimiAI - [REQUEST::request] JSON is {}",reqJson);
            req.set(this.getRequest(httpMethod, body));
        });
        Optional.ofNullable(headers).ifPresent(v-> req.get().headers(Headers.of(v)));
        R r = detailRequest(req.get().build(), responseClass, errorResolve);
        log.debug("KimiAI - [REQUEST::response] JSON is {}",responseResultTempStorage.get());
        return r;
    }

    public <T extends RequestBody,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass,Function<KimiError<T>,Boolean> errorResolve){
        return REQUEST(httpMethod,request,responseClass,null,errorResolve);
    }

    public <T extends RequestBody> EventSource STREAM(T request, Map<String,String>headers, KimiEventSourceLinsener kimiEventSourceLinsener,Function<KimiError<T>,Boolean> errorResolve){
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("text/event-stream"),KimiGsonFactory.create().toJson(request));
        if (StringUtils.isBlank(key)){
            throw new KaToolException(ErrorCode.OPER_ERROR,"kimi-key is null");
        }

        Request.Builder header = new Request.Builder().url(this.kimiBuilder.getUrl());

        if (headers !=null && !headers.isEmpty()){
            header.headers(Headers.of(headers));
        }
        header.addHeader(Header.AUTHORIZATION.getValue(), "Bearer " + key)
                .addHeader("Connection", "keep-alive");
        Request req = header
                .post(requestBody)
                .build();
        RealEventSource eventSource = new RealEventSource(req, kimiEventSourceLinsener);
        eventSource.connect(this.httpUtil.getClient());
        return eventSource;
    }

    public <T extends RequestBody> EventSource STREAM(T request,KimiEventSourceLinsener kimiEventSourceLinsener,Function<KimiError<T>,Boolean> errorResolve){
        return STREAM(request,null,kimiEventSourceLinsener,errorResolve);
    }
    public KimiFileMeta upload(File file,Function<KimiError<KimiFileMeta>,Boolean> errorResolve){
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        Request.Builder post = this.getRequest(Method.POST,null);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("purpose","file-extract");
        builder.addFormDataPart("file", file.getName(), okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file));
        post.post(builder.build());
        return detailRequest(post.build(),KimiFileMeta.class,errorResolve);
    };
    public List<KimiFileMeta> uploadFiles(List<File> files,Function<KimiError<KimiFileMeta>,Boolean> errorResolve) {
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        List<KimiFileMeta> responseList = new ArrayList<>();;
        files.parallelStream().forEach(file->{
            Request.Builder post = this.getRequest(Method.POST,null);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addFormDataPart("purpose","file-extract");
            builder.addFormDataPart("file", file.getName(), okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"), file));
            post.post(builder.build());
            KimiFileMeta fileMeta = detailRequest(post.build(), KimiFileMeta.class, errorResolve);
            responseList.add(fileMeta);
        });
        return responseList;
    }
    public KimiFileMeta upload(File file){
        return upload(file,null);
    };
    public List<KimiFileMeta> uploadFiles(List<File> files){
        return uploadFiles(files,null);
    };
    public <T> T anlayseResponse(String json, TypeToken<T> kimiChatResponseTypeToken) {
        return KimiGsonFactory.create().fromJson(json,kimiChatResponseTypeToken.getType());
    }

}
