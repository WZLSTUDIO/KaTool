package cn.katool.services.ai;
import cn.katool.services.ai.model.drive.PromptTemplateDrive;
import cn.katool.services.ai.model.entity.ErrorMessage;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseResponse;
import cn.katool.services.ai.model.dto.kimi.base.KimiDefaultDeleteResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileContentResponse;
import cn.katool.services.ai.model.dto.kimi.file.KimiFileMeta;
import cn.katool.services.ai.model.dto.kimi.other.AiQueryMoneyResponse;
import cn.katool.services.ai.model.entity.CommonAIMessage;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public interface CommonAIService<EQUERY extends ErrorMessage,EFILE extends ErrorMessage,ECHAT extends ErrorMessage>{
    CommonAIService setProxy(String url,int port);
    CommonAIService auth(List<String> key);
    PromptTemplateDrive getPromptTemplateDrive();
    CommonAIService setPromptTemplateDrive(PromptTemplateDrive promptTemplateDrive);
    CommonAIService setJsonTemplate(String jsonTemplate);
    CommonAIService setJsonTemplate(Object dao);
    String getJsonTemplate();
    CommonAIService claerHistory();
    CommonAIService reload(PromptTemplateDrive drive);
    Long countToken(List<CommonAIMessage> chatRequest, Function<ECHAT,Boolean> errorResolve);
    AiQueryMoneyResponse queryMoney(Function<EQUERY,Boolean> errorResolve);
    String ask(String msg);
    String askWithContext(String msg);
    String askBackJson(String msg);
    String askWithContextBackJson(String msg);
    Object askBackDao(String msg, Type type);
    String ask(String msg, Function<ECHAT,Boolean> errorResolve);
    String askWithContext(String msg, Function<ECHAT,Boolean> errorResolve);
    String askBackJson(String msg, Function<ECHAT,Boolean> errorResolve);
    String askWithContextBackJson(String msg, Function<ECHAT,Boolean> errorResolve);
    Object askBackDao(String msg, Type type, Function<ECHAT,Boolean> errorResolve);
    Object askWithContextBackDao(String msg, Type type);
    String uploadFile(File file);
    List<String> uploadFile(List<File> files);
    String uploadFile(String filePath);
    Object getFileMeta(String fileId);
    List<String> uploadFileOfUrls(List<String> filePaths);
    Object listOfFile();
    Object deleteFile(String fileId);
    Object getFileContent(String fileId);
    Object askWithContextBackDao(String msg, Type type, Function<ECHAT,Boolean> errorResolve);
    String uploadFile(File file, Function<EFILE,Boolean> errorResolve);
    List<String> uploadFile(List<File> files, Function<EFILE,Boolean> errorResolve);
    String uploadFile(String filePath, Function<EFILE,Boolean> errorResolve);
    List<String> uploadFileOfUrls(List<String> filePaths, Function<EFILE,Boolean> errorResolve);
    KimiBaseResponse<List<KimiFileMeta>> listOfFile(Function<EFILE,Boolean> errorResolve);
    KimiFileMeta getFileMeta(String fileId, Function<EFILE,Boolean> errorResolve);
    KimiDefaultDeleteResponse deleteFile(String fileId, Function<EFILE,Boolean> errorResolve);
    KimiFileContentResponse getFileContent(String fileId, Function<EFILE,Boolean> errorResolve);
    Long countToken(Function<EQUERY,Boolean> errorResolve);
    Long countToken();
    Long countToken(List<CommonAIMessage> chatRequest);
    AiQueryMoneyResponse  queryMoney();
}
