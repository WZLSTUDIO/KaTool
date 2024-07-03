package cn.katool.services.ai.server.kimi;

import cn.hutool.http.HttpUtil;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.KimiModel;
import cn.katool.services.ai.constant.KimiResponseFormatEnum;
import cn.katool.services.ai.model.builder.KimiBuilder;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.*;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class KimiAIService implements CommonAIService {

    public KimiAIService() {
        promptTemplateDrive = PromptTemplateDrive.create("你好，请你提问",new HashMap<>());
        history = new CopyOnWriteArrayList<>();
    }

    PromptTemplateDrive promptTemplateDrive;

    List<CommonAIMessage> history;

    String jsonTemplate;
    KimiChatRequest chatRequest = new KimiChatRequest();


    public KimiAIService(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = promptTemplateDrive;
        this.history = new CopyOnWriteArrayList<>();
        this.history.add(promptTemplateDrive.generateTemplate());
    }


    @Override
    public CommonAIService setJsonTemplate(String jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
        return this;
    }

    @Override
    public CommonAIService setJsonTemplate(Object dao) {
        this.jsonTemplate = new Gson().toJson(dao);
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
        this.promptTemplateDrive = drive;
        this.history.add(promptTemplateDrive.generateTemplate());
        return this;
    }

    private static volatile CommonAIMessage lastPrompt;
    private String askAdapter(String msg,boolean usingHistory,boolean returnJson) {

        List<CommonAIMessage> messages;
        if (returnJson) {
            chatRequest.setResponse_format(KimiResponseFormatEnum.JSON);
            msg += "请你按照以下Json格式回复我：\n" +this.getJsonTemplate();
        }
        else {
            chatRequest.setResponse_format(KimiResponseFormatEnum.TEXT);
        }
        if (!usingHistory) {
            messages = new CopyOnWriteArrayList<>();
            messages.addAll(Arrays.asList(promptTemplateDrive.generateTemplate(),new CommonAIMessage(CommonAIRoleEnum.USER, msg)));
        }
        else{
            messages = history;
            messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
        }

        chatRequest.setMessages(messages);
        KimiChatResponse post = KimiBuilder.create().chat().completions().build()
                .post(chatRequest);
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
    public KimiFileMetaResponse listOfFile(){
        return KimiBuilder.create().files().build().listOfFileMetas();
    }

    @Override
    public KimiFileMeta getFileMeta(String fileId){
        return KimiBuilder.create().files().build().getFileMeta(fileId);
    }

    @Override
    public KimiFileDeleteResponse deleteFile(String fileId){
        return KimiBuilder.create().files().build().deleteFile(fileId);
    }

    @Override
    public KimiFileContentResponse getFileContent(String fileId){
        return KimiBuilder.create().files().build().getFileContent(fileId);
    }

    @Override
    public Long countToken(){
        return KimiBuilder.create().tokenizers().estimate_token_count().build().countToken(this.chatRequest);
    }

    @Override
    public Long countToken(List<CommonAIMessage> chatRequest){
        return KimiBuilder.create().tokenizers().estimate_token_count().build()
                .countToken(new KimiChatRequest()
                        .setModel(KimiModel.MOONSHOT_V1_32K)
                        .setMax_tokens(2000000).setMessages(chatRequest));
    }

    @Override
    public KimiOtherResponse.KimiOtherResponseData queryMoney(){
        return KimiBuilder.create().users().me().balance().build().queryMoney();
    }
}
