package cn.katool.util.auth;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.katool.constant.AuthConstant;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.bean.BeanUtil;;
import org.springframework.util.ObjectUtils;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Data
public class AuthUtil<T> {
    static long EXPIRE_TIME;
    static String SALT_KEY;
    public static long getExpireTime() {
        return EXPIRE_TIME;
    }
    public static void setExpireTime(long expireTime) {
        EXPIRE_TIME = expireTime;
    }
    public static String getSaltKey() {
        return SALT_KEY;
    }
    public static void setSaltKey(String saltKey) {
        SALT_KEY = saltKey;
    }
    public static String getToken(HttpServletRequest request){
        if (ObjectUtils.isEmpty(request)){
            throw new RuntimeException("【KaTool::AuthUtil::getToken】request is null");
        }
        return  request.getHeader(AuthConstant.TOKEN_HEADER);
    }
    /**从
     * 生成Token
     * @param payloadObj 用户信息
     * @return token
     */
    public static String createToken(Object payloadObj,Class clazz) {
        // 构建header
        log.debug("【KaTool::AuthUtil::createToken】token.header create begin:{}",payloadObj);
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        long currentTimeMillis = System.currentTimeMillis();
        Date expireDate = new Date(currentTimeMillis + EXPIRE_TIME);
        header.put("expTime",expireDate.getTime());
        log.debug("【KaTool::AuthUtil::createToken】token.header create end and payload begin:{}",payloadObj);
        // 构建payload
        Map<String, Object> payload = new HashMap<>();
        Object payloadEntity = null;
        try {
            payloadEntity = clazz.newInstance();
            BeanUtil.copyProperties(payloadObj,payloadEntity);
            payload.put("body", payloadEntity);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.addSuppressed(new Exception("【KaTool-Excepton Warning::AuthUtil::createToken】请检查payloadObj是否为内部类，如果是，可以尝试改为静态内部类或者变为外部类来解决。"));
            throw new RuntimeException(e);
        }
        log.debug("【KaTool::AuthUtil::createToken】token.header payload end and generate Token begin:{},{},{},{}",header,payload,SALT_KEY,payloadObj);
        // 生成Token
        String token = JWTUtil.createToken(header, payload, SALT_KEY.getBytes());
        log.debug("【KaTool::AuthUtil::createToken】PayLoad:[{}] 生成Token成功！\n" +
                "过期时间为：【{}】\n" +
                "header：\n" +
                "【{}】\n" +
                "payload：\n" +
                "【{}】\n" +
                "Token为：\n" +
                "【{}】", payloadObj,expireDate,header, payload,token);
        return token;
    }
    public static String createToken(Object payloadObj) {
        // 构建header
        log.debug("【KaTool::AuthUtil::createToken】token.header create begin:{}",payloadObj);
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        long currentTimeMillis = System.currentTimeMillis();
        Date expireDate = new Date(currentTimeMillis + EXPIRE_TIME);
        header.put("expTime",expireDate.getTime());
        log.debug("【KaTool::AuthUtil::createToken】token.header create end and payload begin:{}",payloadObj);
        // 构建payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("body", payloadObj);
        log.debug("【KaTool::AuthUtil::createToken】token.header payload end and generate Token begin:{},{},{},{}",header,payload,SALT_KEY,payloadObj);
        // 生成Token
        String token = JWTUtil.createToken(header, payload, SALT_KEY.getBytes());
        log.debug("\n【KaTool::AuthUtil::createToken】PayLoad:[{}] 生成Token成功！\n" +
                "过期时间为：【{}】\n" +
                "header：\n" +
                "【{}】\n" +
                "payload：\n" +
                "【{}】\n" +
                "Token为：\n" +
                "【{}】", payloadObj,expireDate,header, payload,token);
        return token;
    }
    /**
     * 校验Token是否有效
     * @param token Token
     * @return 校验结果
     */
    public static boolean verifyToken(String token) {
        if (StringUtil.isNullOrEmpty(token)||"default".equals(token)){
            return false;
        }
        log.info("【KaTool::AuthUtil::verifyToken】token verify:{}",token);
        boolean verify = JWTUtil.verify(token, SALT_KEY.getBytes());
        if (!verify) {
            log.error("【KaTool::AuthUtil::verifyToken】Token解析失败，请检查Token是否正确");
            return false;
        }
        if (isExpired(token)){
            log.error("【KaTool::AuthUtil::verifyToken】Token已过期，请重新登录");
            return false;
        }
        return true;
    }
    /**
     * 判断Token是否已过期
     * @param token Token
     * @return 过期返回true，未过期返回false
     */
    public static boolean isExpired(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        log.debug("【KaTool::AuthUtil::isExpired】jwt:{}",jwt.getHeader());
        Date expTime = new Date(Long.valueOf(jwt.getHeader("expTime").toString()));
        Date currentTime = new Date();
        boolean isBefore = expTime.before(currentTime);
        log.debug("【KaTool::AuthUtil::isExpired】expTime:{} , Now:{} , isBefore:{}",expTime,currentTime,isBefore);
        return isBefore;
    }
    /**
     * 解析Token返回PayLoad对象
     * @param token Token
     * @return PayLoad对象
     */
    public static Object getPayLoadFromToken(String token,Class clazz) {
        if (!verifyToken(token)){
            return null;
        }
        Object body = JSONUtil.toBean((JSONObject) JWTUtil.parseToken(token).getPayload("body"),clazz);
        if (body == null) {
            log.error("【KaTool::AuthUtil::getPayLoadFromToken】Token解析失败，请检查Token是否正确:{}",token);
            return null;
        }
        return body;
    }
    public T getPayLoadFromToken(String token) {
        if (!verifyToken(token)){
            return null;
        }
        T body = JSONUtil.toBean((JSONObject) JWTUtil.parseToken(token).getPayload("body"),
                (Class<T>) ((ParameterizedType) AuthUtil.class.getGenericSuperclass()).getActualTypeArguments()[0]
                );
        if (body == null) {
            log.error("【KaTool::AuthUtil::getPayLoadFromToken】Token解析失败，请检查Token是否正确:{}",token);
            return null;
        }
        return body;
    }
    public static <T> T getPayLoadByToken(String token) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!verifyToken(token)){
            return null;
        }
        Type genericSuperclass = AuthUtil.class.getGenericSuperclass();
        Class<T> clazz;
        if (genericSuperclass instanceof ParameterizedType){
            clazz = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        }
        else {
            clazz = (Class<T>) Class.forName(genericSuperclass.getTypeName()).getDeclaredConstructors()[0].newInstance().getClass();
        }
        T body = JSONUtil.toBean((JSONObject) JWTUtil.parseToken(token).getPayload("body"),
                clazz
        );
        if (body == null) {
            log.error("【KaTool::AuthUtil::getPayLoadFromToken】Token解析失败，请检查Token是否正确:{}",token);
            return null;
        }
        return body;
    }
}