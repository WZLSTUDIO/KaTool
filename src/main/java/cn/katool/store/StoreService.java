package cn.katool.store;
import cn.katool.Exception.KaToolException;
import com.qiniu.common.QiniuException;
import java.io.File;
import java.io.InputStream;
public interface StoreService {
    String getOriginName(String URL);
    /**
     * 判断文件是否存在
     * @param dir   上传目录
     * @param fileName  文件名
     */
    boolean isExist(String dir,String fileName) throws KaToolException;
    /**
     * 判断文件是否存在
     * @param dir   上传目录
     * @param fileName_fast     文件前缀
     * @param fileName_second   文件后缀名。如:.txt
     */
    boolean isExist(String dir,String fileName_fast, String fileName_second) throws KaToolException;
    /**
     * 以文件的形式上传
     *
     * @param file          文件
     * @param dir   上传目录
     * @param fileName_fast 文件前缀名
     * @param fileName_second 文件后缀名。如:.txt
     * @param isCompulsion 是否强制上传
     * @return java.lang.String 上传成功并返回地址，携带cdn刷新
     * @throws QiniuException 上传异常
     */
    String uploadFile(File file, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception;
    /**
     * 以文件的形式上传
     * @param file      文件
     * @param fileName  文件名
     * @param isCompulsion  是否强制上传
     * @return java.lang.String 上传成功并返回地址，携带cdn刷新
     * @throws QiniuException 上传异常
     */
    String uploadFile(File file, String dir,String fileName,boolean isCompulsion) throws Exception;
    String getUrlByName(String fileName);
    /**
     * 以流的形式上传
     *
     * @param inputStream 输入流
     * @param dir   上传目录
     * @param fileName_fast 文件名
     * @param fileName_second 文件后缀名。如:.txt
     * @param isCompulsion 是否强制上传
     * @return java.lang.String 上传成功并返回地址，携带cdn刷新
     * @throws QiniuException 上传异常
     */
    String uploadFile(InputStream inputStream, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception;
    /**
     * 以流的形式上传
     *
     * @param inputStream 输入流
     * @param dir   上传目录
     * @param fileName 文件名
     * @param isCompulsion 是否强制上传
     * @return java.lang.String 上传成功并返回地址，携带cdn刷新
     * @throws QiniuException 上传异常
     */
    String uploadFile(InputStream inputStream, String dir,String fileName,boolean isCompulsion) throws Exception;
    /**
     * 删除文件
     *
     * @param dir   上传目录
     * @param fileName  文件名
     * @return java.lang.String
     */
    String delete(String dir,String fileName) throws QiniuException;
    /**
     * 删除文件
     * @param dir   上传目录
     * @param fileName_fast 文件名
     * @param fileName_second 文件后缀名。如:.txt
     * @return java.lang.String
     */
    String delete(String dir,String fileName_fast, String fileName_second)throws QiniuException;
}
