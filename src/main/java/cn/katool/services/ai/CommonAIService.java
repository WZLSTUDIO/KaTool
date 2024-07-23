package cn.katool.services.ai;

import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseResponse;
import cn.katool.services.ai.model.dto.kimi.base.KimiDefaultDeleteResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileContentResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import cn.katool.services.ai.model.dto.kimi.other.AiQueryMoneyResponse;
import cn.katool.services.ai.model.dto.kimi.other.KimiOtherResponse;
import cn.katool.services.ai.model.entity.CommonAIMessage;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

public interface CommonAIService<M extends AiQueryMoneyResponse,E extends ErrorMessage> {

    CommonAIService setProxy(String url,int port);

    CommonAIService auth(String key);


    PromptTemplateDrive getPromptTemplateDrive();

    CommonAIService setPromptTemplateDrive(PromptTemplateDrive promptTemplateDrive);

    CommonAIService setJsonTemplate(String jsonTemplate);

    CommonAIService setJsonTemplate(Object dao);


    String getJsonTemplate();

    CommonAIService claerHistory();


    CommonAIService reload(PromptTemplateDrive drive);


    Long countToken(List<CommonAIMessage> chatRequest, Consumer<E> throwResolve);

    M queryMoney(Consumer<E> throwResolve);

    String ask(String msg);

    String askWithContext(String msg);

    String askBackJson(String msg);

    String askWithContextBackJson(String msg);

    Object askBackDao(String msg, Type type);

    String ask(String msg, Consumer<E> throwResolve);

    String askWithContext(String msg, Consumer<E> throwResolve);

    String askBackJson(String msg, Consumer<E> throwResolve);

    String askWithContextBackJson(String msg, Consumer<E> throwResolve);

    Object askBackDao(String msg, Type type, Consumer<E> throwResolve);

    Object askWithContextBackDao(String msg, Type type);

    String uploadFile(File file);


    List<String> uploadFile(List<File> files);

    String uploadFile(String filePath);


    Object getFileMeta(String fileId);


    List<String> uploadFileOfUrls(List<String> filePaths);

    Object listOfFile();

    Object deleteFile(String fileId);

    Object getFileContent(String fileId);

    Object askWithContextBackDao(String msg, Type type, Consumer<E> throwResolve);

    String uploadFile(File file, Consumer<E> throwResolve);

    List<String> uploadFile(List<File> files, Consumer<E> throwResolve);

    String uploadFile(String filePath, Consumer<E> throwResolve);

    List<String> uploadFileOfUrls(List<String> filePaths, Consumer<E> throwResolve);

    KimiBaseResponse<List<KimiFileMeta>> listOfFile(Consumer<E> throwResolve);

    KimiFileMeta getFileMeta(String fileId, Consumer<E> throwResolve);

    KimiDefaultDeleteResponse deleteFile(String fileId, Consumer<E> throwResolve);

    KimiFileContentResponse getFileContent(String fileId, Consumer<E> throwResolve);

    Long countToken(Consumer<E> throwResolve);

    Long countToken();

    Long countToken(List<CommonAIMessage> chatRequest);

    KimiOtherResponse.KimiOtherResponseData queryMoney();
}
