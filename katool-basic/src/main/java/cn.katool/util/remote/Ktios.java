package cn.katool.util.remote;

import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.services.ai.acl.kimi.KimiGsonFactory;
import cn.katool.util.AiServiceHttpUtil;
import cn.katool.util.remote.formatChain.EnDeCodeFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Ktios {
    private EnDeCodeFormat decodeFormat;

    public Ktios setDecodeFormat(EnDeCodeFormat decodeFormat) {
        this.decodeFormat = decodeFormat;
        return this;
    }

    public static class Then {
        public static class err<T>{
            public ResponseDao<T> data = null;
            public err(ResponseDao body) {
                this.data = body;
            }

        }
        public static class res<T>{
            public ResponseDao<T> data = null;
            public res(ResponseDao body) {
                this.data = body;
            }
        }
        private Response response = null;

        public Then(Response response) {
            this.response = response;
        }

        public <T> T exec(Class<T> clazz){
            return then(clazz, (err,res)->{
                if(err != null){
                    throw new KaToolException(ErrorCode.OPER_ERROR,err.data.msg);
                }
                return res.data.data;
            });
        }
        @SneakyThrows
        public Map<String,String> then(BiFunction<err<Map<String,String>>,res<Map<String,String>>,Map<String,String>> consumer) {
            ResponseBody body = response.body();
            ResponseDao<Map<String,String>> data= KimiGsonFactory.create().fromJson(body.string(),TypeToken.get(new ResponseDao().getClass()).getType());

            if(response.isSuccessful()){
                return consumer.apply(null, new res<Map<String,String>>(data));
            }
            else{
                return consumer.apply(new err<Map<String,String>>(data), null);
            }
        }
        @SneakyThrows
        public <T> T then(Class<T> clazz,BiFunction<err<T>, res<T>,T> consumer) {
            ResponseBody body = response.body();
            ResponseDao<Map<String,String>> data= KimiGsonFactory.create().fromJson(body.string(),TypeToken.get(new ResponseDao().getClass()).getType());
            ResponseDao<T> tResponseDao = new ResponseDao<>();
            tResponseDao.code = data.code;
            tResponseDao.msg = data.msg;
            tResponseDao.data = null;
            if (null!=data.data) {
                tResponseDao.data = KimiGsonFactory.create().fromJson(new Gson().toJson(data.data), TypeToken.get(clazz).getType());
            }
            if(response.isSuccessful()){
                return consumer.apply(null, new res<T>(tResponseDao));
            }
            else{
                return consumer.apply(new err<T>(tResponseDao), null);
            }
        }
    }
    private volatile OkHttpClient httpClient = new AiServiceHttpUtil().createHttpClient();

    private volatile String baseURL = "";

    private volatile Map<String, String> headers = new HashMap<>();

    public Ktios(String baseURL) {
        this.baseURL = baseURL;
    }
    private Request.Builder getRequest(String url) {
        if (baseURL.charAt(baseURL.length() - 1) != '/'){
            baseURL+="/";
        }
        if (url.charAt(0) == '/'){
            url = url.substring(1);
        }
        return new Request.Builder().url(baseURL + url).headers(Headers.of(headers));
    }
    @SneakyThrows
    public  Then get(String url){
        Request build = getRequest(url).get().build();
        Response execute = httpClient.newCall(build).execute();
        return new Then(execute);
    }
    public  Then get(String url,Map<String,String> prama) {
        if (prama != null){
            url += "?";
            for (Map.Entry<String, String> entry : prama.entrySet()) {
                url += entry.getKey() + "=" + entry.getValue() + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        return get(url);
    }

    @SneakyThrows
    public  Then post(String url,Object data) {
        Request request = this.getRequest(url)
                .post(RequestBody.create(MediaType.parse("application/json"), KimiGsonFactory.create().toJson(data))).build();
        Response execute = httpClient.newCall(request).execute();
        return new Then(execute);
    }
    @SneakyThrows
    public  Then put(String url,Object data,Boolean async) {
        Request request = this.getRequest(url)
                .put(RequestBody.create(MediaType.parse("application/json"), KimiGsonFactory.create().toJson(data))).build();
        Call call = httpClient.newCall(request);
        if (async){
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    call.cancel();
                    response.close();
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                }

            });
        }
        else{
            Response execute = call.execute();
            return new Then(execute);
        }
        return null;
    }
    @SneakyThrows
    public  Then put(String url,Object data) {
        return put(url,data,false);
    }
    @SneakyThrows
    public  Then delete(String url) {
        Request request = this.getRequest(url)
                .delete().build();
        Response execute = httpClient.newCall(request).execute();
        return new Then(execute);
    }
    @SneakyThrows
    public  Then delete(String url,Map<String, String> prama) {
        Request request = this.getRequest(url)
                .delete().build();
        Response execute = httpClient.newCall(request).execute();
        return new Then(execute);
    }
    @SneakyThrows
    public  Then delete(String url,Object data) {
        Request request = this.getRequest(url)
                .delete(RequestBody.create(KimiGsonFactory.create().toJson(data).getBytes())).build();
        Response execute = httpClient.newCall(request).execute();
        return new Then(execute);
    }

    public Ktios headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }
    public Ktios header(String key, String value) {
        this.headers.put(key,value);
        return this;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public Ktios setBaseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }
}
