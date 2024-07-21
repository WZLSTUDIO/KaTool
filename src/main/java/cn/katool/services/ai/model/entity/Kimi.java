package cn.katool.services.ai.model.entity;

import cn.hutool.http.*;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.services.ai.constant.kimi.KimiBuilderEnum;
import cn.katool.services.ai.model.builder.KimiBuilder;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Kimi{

    KimiBuilder kimiBuilder;

    String key;

    InheritableThreadLocal<String> contentType = new InheritableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "application/json";
        }
    };
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
    private <T> T detailRequest(HttpRequest request,Type responseClass){
        HttpResponse httpResponse = request.execute();
        String resJson = httpResponse.body();
        if (!httpResponse.isOk()){
            throw new KaToolException(ErrorCode.OPER_ERROR,resJson);
        }
        T res = KimiGsonFactory.create().fromJson(resJson, TypeToken.get(responseClass).getType());
        return res;
    }

    public <T,R> R POST(T request,Type responseClass){
        validUnLegal(KimiBuilderEnum.FILES);
        HttpRequest post = this.getRequest(Method.POST).body(KimiGsonFactory.create().toJson(request));
        return detailRequest(post,responseClass);
    }
    public <T> T GET(Type resposneClass){
        HttpRequest request = this.getRequest(Method.GET);
        return detailRequest(request, resposneClass);
    }
    public <T> T DELETE(Type responseClass){
        HttpRequest request = this.getRequest(Method.DELETE);
        return detailRequest(request, responseClass);
    }

    public <T,R> R PUT(T request,Type responseClass){
        HttpRequest put = this.getRequest(Method.PUT).body(KimiGsonFactory.create().toJson(request));
        return detailRequest(put,responseClass);
    }
    public <T,R> R POST(T request,Class<R> responseClass){
        return POST(request,(Type)responseClass);
    }
    public <T> T GET(Class<T> resposneClass){
        return GET((Type) resposneClass);
    }
    public <T> T DELETE(Class<T>  responseClass){
        return DELETE( (Type) responseClass);
    }
    public <T,R> R PUT(T request,Class<R> responseClass){
        return PUT(request,(Type)responseClass);
    }

    public <T,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass,Map<String,String>headers){
        HttpRequest req = this.getRequest(httpMethod);
        Optional.ofNullable(headers).ifPresent(req::addHeaders);
        Optional.ofNullable(request).ifPresent(body->{
            req.body(KimiGsonFactory.create().toJson(body));
        });
        return detailRequest(req,responseClass);
    }
    public <T,R> R REQUEST(Method httpMethod,T request,Class<R> responseClass){
        return REQUEST(httpMethod,request,responseClass,null);
    }

    public KimiFileMeta upload(File file){
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        HttpRequest post = this.getRequest(Method.POST);
        post.form("purpose","file-extract");
        post.form("file", file);
        return detailRequest(post,KimiFileMeta.class);
    };


    public List<KimiFileMeta> uploadFiles(List<File> files) {
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        List<HttpResponse> responseList = new ArrayList<>();;
        files.parallelStream().forEach(file->{
            HttpRequest post = this.getRequest(Method.POST);
            post.form("purpose", "file-extract");
            post.form("file", file);
            HttpResponse httpResponse = post.execute();
            if (!httpResponse.isOk()){
                throw new KaToolException(ErrorCode.OPER_ERROR, httpResponse.body());
            }
            responseList.add(httpResponse);
        });
        CopyOnWriteArrayList<KimiFileMeta> kimiFileMetaList = new CopyOnWriteArrayList<>();
        responseList.parallelStream()
                .forEach(item->kimiFileMetaList
                        .add(KimiGsonFactory.create().fromJson(item.body(), new TypeToken<KimiFileMeta>(){}.getType())));
        return kimiFileMetaList.stream().collect(Collectors.toList());
    }

}
