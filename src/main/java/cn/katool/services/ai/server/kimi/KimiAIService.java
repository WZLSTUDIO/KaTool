package cn.katool.services.ai.server.kimi;

import cn.hutool.http.HttpUtil;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.constant.KimiResponseFormatEnum;
import cn.katool.services.ai.model.builder.KimiBuilder;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.*;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiAIService implements CommonAIService {


    PromptTemplateDrive promptTemplateDrive;

    List<CommonAIMessage> history;

    String jsonTemplate;
    KimiChatRequest chatRequest;


    public KimiAIService(PromptTemplateDrive promptTemplateDrive) {
        this.promptTemplateDrive = promptTemplateDrive;
        this.history = new CopyOnWriteArrayList<>();
        this.history.add(promptTemplateDrive.generateTemplate());
    }


    @Override
    public void setJsonTemplate(String jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
    }

    @Override
    public void setJsonTemplate(Object dao) {
        this.jsonTemplate = new Gson().toJson(dao);
    }

    @Override
    public void claerHistory() {
        this.history.clear();
    }

    @Override
    public void reload(PromptTemplateDrive drive) {
        this.claerHistory();
        this.promptTemplateDrive = drive;
        this.history.add(promptTemplateDrive.generateTemplate());
    }
    private String askAdapter(String msg,boolean usingHistory,boolean returnJson) {
        if (returnJson) {
            chatRequest.setResponse_format(KimiResponseFormatEnum.JSON);
        }
        List<CommonAIMessage> messages;
        if (!usingHistory) {
            messages = Arrays.asList(promptTemplateDrive.generateTemplate(),new CommonAIMessage(CommonAIRoleEnum.USER, msg));
        }
        else{
            messages = history;
            messages.add(new CommonAIMessage(CommonAIRoleEnum.USER, msg));
        }
        chatRequest.setMessages(messages);
        KimiChatResponse post = KimiBuilder.create().chat().completions().build()
                .post(chatRequest);
        CommonAIMessage message = post.getChoices().get(0).getMessage();
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
    public String uploadFile(String filePath) {
        byte[] bytes = HttpUtil.downloadBytes(filePath);
        KimiFileMeta upload = KimiBuilder.create().files().build().upload(bytes);
        return upload.getId();
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
}
