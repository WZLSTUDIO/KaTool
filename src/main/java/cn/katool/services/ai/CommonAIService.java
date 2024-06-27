package cn.katool.services.ai;

import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.*;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

public interface CommonAIService {


    PromptTemplateDrive getPromptTemplateDrive();

    void setPromptTemplateDrive(PromptTemplateDrive promptTemplateDrive);

    void setJsonTemplate(String jsonTemplate);

    void setJsonTemplate(Object dao);


    String getJsonTemplate();

    void claerHistory();


    void reload(PromptTemplateDrive drive);


    String ask(String msg);

    String askWithContext(String msg);

    String askBackJson(String msg);

    String askWithContextBackJson(String msg);

    Object askBackDao(String msg, Type type);

    Object askWithContextBackDao(String msg,Type type);

    String uploadFile(File file);


    List<String> uploadFile(List<File> files);

    String uploadFile(String filePath);


    Object getFileMeta(String fileId);


    List<String> uploadFileOfUrls(List<String> filePaths);

    Object listOfFile();

    Object deleteFile(String fileId);

    Object getFileContent(String fileId);

    Long countToken();

    Long countToken(List<CommonAIMessage> chatRequest);

    KimiOtherResponse.KimiOtherResponseData queryMoney();
}
