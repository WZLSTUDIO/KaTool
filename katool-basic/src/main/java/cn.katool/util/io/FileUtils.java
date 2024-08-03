/**
 * Title
 *
 * @ClassName: FileUtils
 * @Description:
 * @author: Karos
 * @date: 2023/1/2 1:52
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.util.io;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
@Component
@Slf4j
public class FileUtils {
    public static File createTempFile(){
        return createTempFile(true);
    }
    public static File createTempFile(boolean isReCreate){
        int num=0;
        while (true) {
            try {
                File tempFile = File.createTempFile("Katool", (String) null, (File) null).getCanonicalFile();
                if (isReCreate==true){
                   tempFile.delete();
                   tempFile.createNewFile();
                }
                log.debug("【KaTool::FileUtils】 =>  Info: 临时文件创建成功");
                return tempFile;
            } catch (IOException e) {
                log.warn("【KaTool::FileUtils】 =>  Warn: 临时文件创建重试");
                if (num++>50){
                    log.error("【KaTool::FileUtils】 =>  Error: 临时文件创建失败");
                    throw new KaToolException(ErrorCode.FILE_ERROR);
                }
            }
        }
    }
}
