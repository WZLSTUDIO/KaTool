/**
 * Title
 *
 * @ClassName: QiniuServiceImpl
 * @Description:七牛云服务实例
 * @author: Karos
 * @date: 2022/12/13 21:55
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.store.impl.qiniu;
import cn.katool.util.io.FileUtils;
import cn.katool.store.interfaces.IQiNiuYunService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.InputStream;
@Data
@Service("Store-QiNiuYun")
@Slf4j
public class QiNiuYunServiceImpl implements IQiNiuYunService, InitializingBean {
    @Autowired
    private UploadManager uploadManager;
    @Autowired
    private BucketManager bucketManager;
    @Autowired
    private Auth auth;
    @Value("${katool.qiniu.bucket:\"your bucket-name\"}")
    private String bucket;
    @Value("${katool.qiniu.domain:\"your domain\"}")
    private String domain;
    @Value("${katool.qiniu.basedir:\"/katool\"}")
    private String basedir;
    /**
     * 定义七牛云上传的相关策略
     */
    private StringMap putPolicy;
    @Override
    public String getOriginName(String URL) {
        int endpos = URL.lastIndexOf("?datestamp");
        int beginpos = URL.lastIndexOf('/');
        String originName = URL.substring(beginpos+1, endpos);
        return originName;
    }
    @Override
    public boolean isExist(String dir,String fileName){
        File tempFile = FileUtils.createTempFile();
        try {
            uploadFile(tempFile,dir,fileName,false);
        } catch (QiniuException e) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            delete(dir,fileName);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean isExist(String dir,String fileName_fast, String fileName_second){
        return isExist(dir,fileName_fast+fileName_second);
    }
    /**
     *
     * @param file
     * @param fileName_fast 文件名
     * @param fileName_second 文件后缀
     * @return
     * @throws QiniuException
     */
    @Override
    public String uploadFile(File file, String dir,String fileName_fast, String fileName_second,boolean isCompulsion) throws Exception {
        String fileName = fileName_fast + fileName_second;
        return uploadFile(file,dir,fileName,isCompulsion);
    }
    @Override
    public String uploadFile(File file, String dir,String fileName,boolean isCompulsion) throws Exception {
        if (isCompulsion){
            if (isExist(dir,fileName)) {
                delete(dir,fileName);
            }
        }
        String mdir="";
        if (basedir!=null){
            if(basedir.charAt(0)=='/') {
                basedir=basedir.substring(1);
            }
            mdir+=basedir+'/';
        }
        if (dir!=null){
            if (dir.charAt(0)=='/') {
                dir=dir.substring(1);
            }
            mdir+=dir+'/';
        }
        fileName=mdir+fileName;
        Response response = this.uploadManager.put(file, fileName, getUploadToken());
        int retry = 0;
        while (response.needRetry() && retry < 3) {
            response = this.uploadManager.put(file, fileName, getUploadToken());
            retry++;
        }
        if (response.statusCode == 200) {
            //通过时间戳实现CDN缓存强制刷新
            //https://developer.qiniu.com/fusion/kb/1325/refresh-the-cache-and-the-effect-of-time
            fileName+="?datestamp="+System.currentTimeMillis();
            log.info("UPLOAD -- > 七牛云:"+"http://" + domain + "/" + fileName);
            return "http://" + domain + "/" + fileName;
        }
        return "上传失败!";
    }
    @Override
    public String getUrlByName(String fileName) {
        return getDomain()+fileName;
    }
    @Override
    public String uploadFile(InputStream inputStream, String dir,String fileName_fast, String fileName_second,boolean isCompulsion) throws Exception {
        String fileName = fileName_fast + fileName_second;
        return uploadFile(inputStream,dir,fileName,isCompulsion);
    }
    @Override
    public String uploadFile(InputStream inputStream, String dir,String fileName,boolean isCompulsion) throws Exception {
        if (isCompulsion){
            if (isExist(dir,fileName)) {
                delete(dir,fileName);
            }
        }
        String mdir="";
        if (basedir!=null){
            if(basedir.charAt(0)=='/') {
                basedir=basedir.substring(1);
            }
            mdir+=basedir+'/';
        }
        if (dir!=null){
            if (dir.charAt(0)=='/') {
                dir=dir.substring(1);
            }
            mdir+=dir+'/';
        }
        fileName=mdir+fileName;
        Response response = this.uploadManager.put(inputStream, fileName, getUploadToken(), null, null);
        int retry = 0;
        while (response.needRetry() && retry < 3) {
            response = this.uploadManager.put(inputStream, fileName, getUploadToken(), null, null);
            retry++;
        }
        if (response.statusCode == 200) {
            //通过时间戳实现CDN缓存强制刷新
            //https://developer.qiniu.com/fusion/kb/1325/refresh-the-cache-and-the-effect-of-time
            fileName+="?datestamp="+System.currentTimeMillis();
            log.info("UPLOAD -- > 七牛云:"+"http://" + domain + "/" + fileName);
            return "http://" + domain + "/" + fileName;
        }
        return "上传失败!";
    }
    /**
     *
     * @param fileName   文件名
     * @return
     * @throws QiniuException
     */
    @Override
    public String delete(String dir,String fileName) throws QiniuException {
        String mdir="";
        if (basedir!=null){
            if(basedir.charAt(0)=='/') {
                basedir=basedir.substring(1);
            }
            mdir+=basedir+'/';
        }
        if (dir!=null){
            if (dir.charAt(0)=='/') {
                dir=dir.substring(1);
            }
            mdir+=dir+'/';
        }
        fileName=mdir+fileName;
        Response response = bucketManager.delete(this.bucket, fileName);
        int retry = 0;
        while (response.needRetry() && retry++ < 3) {
            response = bucketManager.delete(bucket, fileName);
        }
        return response.statusCode == 200 ? "删除成功!" : "删除失败!";
    }
    @Override
    public String delete(String dir,String fileName_fast, String fileName_second) throws QiniuException {
        String fileName=fileName_fast+fileName_second;
        return delete(dir,fileName);
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        this.putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    }
    /**
     * 获取上传凭证
     */
    private String getUploadToken() {
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }
}
