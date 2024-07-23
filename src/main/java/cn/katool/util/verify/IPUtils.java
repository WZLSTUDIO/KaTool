/**
 * Title
 *
 * @ClassName: IpUtils
 * @Description:
 * @author: Karos
 * @date: 2022/12/18 22:41
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.util.verify;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.StringJoiner;
@Component
public class IPUtils {
    /**
     * 获取反向代理IP
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    /**
     * IP地址合法性校验
     */
    public static Boolean isIp(String ipAddr){
        // 对IpAddr进行校验
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        return ipAddr.matches(regex);
    }
}
