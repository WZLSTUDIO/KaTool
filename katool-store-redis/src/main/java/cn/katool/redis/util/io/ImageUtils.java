/**
 * Title
 *
 * @ClassName: IImageUtil
 * @Description:
 * @author: Karos
 * @date: 2022/12/15 21:28
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.redis.util.io;
import cn.hutool.core.img.ImgUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
@Component
public class ImageUtils {
    /**
     * Base64转图片
     * @param base64
     * @return
     * @throws IOException
     */
    public static File base642img(String base64) throws IOException {
        if (base64.length()>=11&"data:image/".equals(base64.substring(0,11))){
            base64=base64.substring(base64.indexOf(',')+1);
        }
        byte[] decode = Base64Utils.decode(base64.getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < decode.length; i++) {
            if (decode[i]<0)decode[i]+=256; //调整异常
        }
        File tempFile = File.createTempFile("temp",".png");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(decode);
        fos.flush();
        fos.close();
        return tempFile;
    }
    public static String img2base64(File image) throws IOException {
        FileInputStream fis= new FileInputStream(image);
        byte b[]=new byte[(int) image.length()];
        fis.read(b);
        String encode = new String(Base64Utils.encode(b));
        return encode;
    }
    /**
     * 将图片放到输出流对象
     * @param src
     * @param os
     * @throws MalformedURLException
     */
    public static void img2fileToOutputStream(String src,OutputStream os) throws MalformedURLException {
        URL url=new URL(src);
        Image image=ImgUtil.getImage(url);
//        Image image= ImageIO.read(url);
        ImgUtil.write(image,ImgUtil.IMAGE_TYPE_PNG, os);
    }
}
