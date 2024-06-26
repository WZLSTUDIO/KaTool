package cn.katool.services.ai.model.entity;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.*;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.constant.KimiBuilderEnum;
import cn.katool.services.ai.model.builder.KimiBuilder;
import cn.katool.services.ai.model.dto.kimi.*;
import com.alibaba.excel.util.FileUtils;
import com.alibaba.excel.util.ListUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.HttpConstants;
import jdk.nashorn.internal.parser.TokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.spi.CopyOnWrite;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Kimi{

    KimiBuilder kimiBuilder;

    String key;

    String contentType;
    private void validUnLegal(KimiBuilderEnum kimiBuilderEnum) {
        if (this.getKimiBuilder().getMaster().equals(kimiBuilderEnum)){
            throw new KaToolException(ErrorCode.OPER_ERROR,Thread.currentThread().getStackTrace()[1].getMethodName()+" method is not supported in file mode");
        }
    }

    private void validLegal(KimiBuilderEnum kimiBuilderEnum) {
        if (!this.getKimiBuilder().getMaster().equals(kimiBuilderEnum)){
            throw new KaToolException(ErrorCode.OPER_ERROR,Thread.currentThread().getStackTrace()[1].getMethodName()+" method is not supported in file mode");
        }
    }

    public Kimi auth(String key){
        this.key = key;
        return this;
    }

    public Kimi contentType(String type){
        this.contentType = type;
        return this;
    }
    private HttpRequest getRequest(Method method){
        HttpRequest request = HttpUtil.createRequest(method,kimiBuilder.getUrl().toString());
        request.contentType(this.contentType)
                .header(Header.AUTHORIZATION,"Bearer " + key);
        return request;
    }
    public KimiChatResponse post(KimiChatRequest kimiChatRequest){
        validUnLegal(KimiBuilderEnum.FILES);
        HttpRequest post = getRequest(Method.POST);
        HttpResponse response = post.body(new Gson().toJson(kimiChatRequest))
                .execute();
        String resJson = response.body();
        KimiChatResponse res = new Gson().fromJson(resJson, new TypeToken<KimiChatResponse>() {
        }.getType());
        return res;
    }


    public KimiFileMeta upload(File file){
        KimiFileMeta kimiFileMeta = null;
        try {
            kimiFileMeta = this.upload(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return kimiFileMeta;
    };

    public KimiFileMeta upload(byte[] bytes){
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        HttpRequest post = getRequest(Method.POST);
        post.form().put("purpose","file-extract");
        post.form().put("file", bytes);
        HttpResponse response = post.execute();
        KimiFileMeta kimiFileMeta = new Gson().fromJson(response.body(), new TypeToken<KimiFileMeta>(){}.getType());
        return kimiFileMeta;
    };

    public List<KimiFileMeta> uploadFiles(List<File> files) {
        List<byte[]> fileByteList = files.parallelStream().map(file -> {
            try {
                return FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return uploadFiles(ArrayUtil.toArray(fileByteList,byte[].class));
    }

    public List<KimiFileMeta> uploadFiles(byte[][] bytesArray) {
        validLegal(KimiBuilderEnum.FILES);
        this.contentType("multipart/form-data");
        List<HttpResponse> responseList = new ArrayList<>();
        List<byte[]> bytesList = Arrays.asList(bytesArray);
        bytesList.parallelStream().forEach(bytes->{
            HttpRequest post = getRequest(Method.POST);
            post.form().put("purpose", "file-extract");
            post.form().put("file", bytes);
            HttpResponse httpResponse = post.execute();
            responseList.add(httpResponse);
        });
        CopyOnWriteArrayList<KimiFileMeta> kimiFileMetaList = new CopyOnWriteArrayList<>();
        responseList.parallelStream()
                .forEach(item->kimiFileMetaList
                        .add(new Gson().fromJson(item.body(), new TypeToken<KimiFileMeta>(){}.getType())));
        return kimiFileMetaList.stream().collect(Collectors.toList());
    }

    public KimiFileMetaResponse listOfFileMetas(){
        HttpRequest request = getRequest(Method.GET);
        HttpResponse response = request.execute();
        return new Gson().fromJson(response.body(),new TypeToken<KimiFileMetaResponse>(){}.getType());
    }

    public KimiFileDeleteResponse deleteFile(String fileId){
        HttpRequest request = getRequest(Method.DELETE);
        HttpResponse response = request.setUrl(request.getUrl() + "/" + fileId).execute();
        return new Gson().fromJson(response.body(), new TypeToken<KimiFileDeleteResponse>(){}.getType());
    }

    public KimiFileMeta getFileMeta(String fileId){
        HttpRequest request = getRequest(Method.GET);
        HttpResponse response = request.setUrl(request.getUrl() + "/" + fileId).execute();
        return new Gson().fromJson(response.body(), new TypeToken<KimiFileMeta>(){}.getType());
    }

    public KimiFileContentResponse getFileContent(String fileId){
        HttpRequest request = getRequest(Method.GET);
        HttpResponse response = request.setUrl(request.getUrl() + "/" + fileId+"/content").execute();
        return new Gson().fromJson(response.body(), new TypeToken<KimiFileContentResponse>(){}.getType());
    }

}
