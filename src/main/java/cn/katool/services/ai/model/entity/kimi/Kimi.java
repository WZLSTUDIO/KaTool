package cn.katool.services.ai.model.entity.kimi;

import cn.hutool.http.*;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.constant.kimi.KimiBuilderEnum;
import cn.katool.services.ai.model.builder.kimi.KimiBuilder;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Kimi{

    KimiBuilder kimiBuilder;

    String key;

    TransmittableThreadLocal<String> contentType = new TransmittableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "application/json";
        }
    };
    TransmittableThreadLocal<String> responseResultTempStorage = new TransmittableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "{}";
        }
    };

    public Kimi(KimiBuilder kimiBuilder, String key, TransmittableThreadLocal<String> contentType) {
        this.kimiBuilder = kimiBuilder;
        this.key = key;
        this.contentType = contentType;
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
        this.key = key;
        return this;
    }

    public Kimi contentType(String type){
        this.contentType.set(type);
        return this;
    }
    private HttpRequest getRequest(Method method){
        if (StringUtils.isBlank(key)){
            throw new KaToolException(ErrorCode.OPER_ERROR,"kimi-key is null");
        }
        HttpRequest request = HttpUtil.createRequest(method,kimiBuilder.getUrl().toString());
        request.contentType(this.contentType.get())
                .header(Header.AUTHORIZATION,"Bearer " + key);
        return request;
    }
    private <T> T detailRequest(HttpRequest request, Type responseClass, Consumer<KimiErrorMessage> throwResolve){
        HttpResponse httpResponse = request.execute();
        String resJson = httpResponse.body();
        if (!httpResponse.isOk()){
            KimiError t = KimiGsonFactory.create().fromJson(resJson, KimiError.class);
            Optional.ofNullable(throwResolve).map(error->{
                log.error("kimi request error:{}",t.getError().getMessage());
                error.accept(t.getError());
                return null;
            }).orElseGet(()->{
                throw new KaToolException(ErrorCode.OPER_ERROR,t.getError().getMessage());
            });
        }
        responseResultTempStorage.set(resJson);
        T res = KimiGsonFactory.create().fromJson(resJson, TypeToken.get(responseClass).getType());
        return res;
    }

    public <T,R> R POST(T request,Type responseClass,Consumer<KimiErrorMessage> throwResolve){
        validUnLegal(KimiBuilderEnum.FILES);
        HttpRequest post = this.getRequest(Method.POST).body(KimiGsonFactory.create().toJson(request));
        return detailRequest(post,responseClass,throwResolve);
    }
    public <T> T GET(Type resposneClass,Consumer<KimiErrorMessage> throwResolve){
        HttpRequest request = this.getRequest(Method.GET);
        return detailRequest(request, resposneClass,throwResolve);
    }
    public <T> T DELETE(Type responseClass,Consumer<KimiErrorMessage> throwResolve){
        HttpRequest request = this.getRequest(Method.DELETE);
        return detailRequest(request, responseClass,throwResolve);
    }

    public <T,R> R PUT(T request,Type responseClass,Consumer<KimiErrorMessage> throwResolve){
        HttpRequest put = this.getRequest(Method.PUT).body(KimiGsonFactory.create().toJson(request));
        return detailRequest(put,responseClass,throwResolve);
    }
    public <T,R> R POST(T request,Class<R> responseClass,Consumer<KimiErrorMessage> throwResolve){
        return POST(request,(Type)responseClass,throwResolve);
    }
    public <T> T GET(Class<T> resposneClass,Consumer<KimiErrorMessage> throwResolve){
        return GET((Type) resposneClass,throwResolve);
    }
    public <T> T DELETE(Class<T>  responseClass,Consumer<KimiErrorMessage> throwResolve){
        return DELETE( (Type) responseClass,throwResolve);
    }
    public <T,R> R PUT(T request,Class<R> responseClass,Consumer<KimiErrorMessage> throwResolve){
        return PUT(request,(Type)responseClass,throwResolve);
    }

    public <T,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass,Map<String,String>headers,Consumer<KimiErrorMessage> throwResolve){
        HttpRequest req = this.getRequest(httpMethod);
        Optional.ofNullable(headers).ifPresent(req::addHeaders);
        Optional.ofNullable(request).ifPresent(body->{
            req.body(KimiGsonFactory.create().toJson(body));
        });
        return detailRequest(req,responseClass,throwResolve);
    }
    public <T,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass,Consumer<KimiErrorMessage> throwResolve){
        return REQUEST(httpMethod,request,responseClass,null,throwResolve);
    }

    public KimiFileMeta upload(File file,Consumer<KimiErrorMessage> throwResolve){
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        HttpRequest post = this.getRequest(Method.POST);
        post.form("purpose","file-extract");
        post.form("file", file);
        return detailRequest(post,KimiFileMeta.class,throwResolve);
    };


    public List<KimiFileMeta> uploadFiles(List<File> files,Consumer<KimiErrorMessage> throwResolve) {
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        List<KimiFileMeta> responseList = new ArrayList<>();;
        files.parallelStream().forEach(file->{
            HttpRequest post = this.getRequest(Method.POST);
            post.form("purpose", "file-extract");
            post.form("file", file);
            KimiFileMeta fileMeta = detailRequest(post, KimiFileMeta.class, throwResolve);
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
