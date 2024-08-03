package cn.katool.store.impl.tencent;
import cn.katool.Exception.KaToolException;
import cn.katool.store.interfaces.ICloudTencentStoreService;
import com.qiniu.common.QiniuException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.InputStream;
@Data
@Service("Store-Tencent")
@Slf4j
public class CloudTencentStoreService implements ICloudTencentStoreService {
    @Override
    public String getOriginName(String URL) {
        return null;
    }
    @Override
    public boolean isExist(String dir, String fileName){
        return false;
    }
    @Override
    public boolean isExist(String dir, String fileName_fast, String fileName_second){
        return false;
    }
    @Override
    public String uploadFile(File file, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception {
        return null;
    }
    @Override
    public String uploadFile(File file, String dir, String fileName, boolean isCompulsion) throws Exception {
        return null;
    }
    @Override
    public String getUrlByName(String fileName) {
        return null;
    }
    @Override
    public String uploadFile(InputStream inputStream, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception {
        return null;
    }
    @Override
    public String uploadFile(InputStream inputStream, String dir, String fileName, boolean isCompulsion) throws Exception {
        return null;
    }
    @Override
    public String delete(String dir, String fileName) throws QiniuException {
        return null;
    }
    @Override
    public String delete(String dir, String fileName_fast, String fileName_second) throws QiniuException {
        return null;
    }
}
