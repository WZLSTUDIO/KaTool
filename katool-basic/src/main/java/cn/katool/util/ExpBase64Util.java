/**
 * Title
 *
 * @ClassName: expBase64Util
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/2/9 1:12
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.util;
import org.springframework.util.Base64Utils;
public class ExpBase64Util extends Base64Utils {
    //Base64 code regular judgment expression
    private static final String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    /**
     * To determine whether a string is a base64 expression, you can use the Base64 header
     *
     * @param base64str
     * @return Returns a result indicating whether the current base64str meets the base64 code
     */
    public static boolean isBase64(String base64str) {
        if (base64str == null || base64str.length() == 0) {
            return false;
        }
        if (base64str.length() >= 11 & "data:image/".equals(base64str.substring(0, 11))) {
            base64str = base64str.substring(base64str.indexOf(',') + 1);
        }
        return base64str.matches(BASE64_PATTERN);
    }
}
