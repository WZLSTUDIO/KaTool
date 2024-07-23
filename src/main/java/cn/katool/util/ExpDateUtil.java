/**
 * Title
 *
 * @ClassName: expDateUtil
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/1/29 2:16
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.util;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import io.netty.util.internal.StringUtil;
import org.springframework.util.ObjectUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class ExpDateUtil extends DateUtil {
    public static String getCorn(Date date){
        if (ObjectUtils.isEmpty(date)) {
            throw new KaToolException(ErrorCode.PARAMS_ERROR,"【KaTool::ExpDateUtil】 getCorn =>  请传入一个正确的date");
        }
        SimpleDateFormat simpleDateFormat=newSimpleFormat("ss mm HH dd MM ? yyyy");
        return simpleDateFormat.format(date);
    }
    public static Date getDate(String corn) throws KaToolException, ParseException {
        if (StringUtil.isNullOrEmpty(corn)) {
            throw new KaToolException(ErrorCode.PARAMS_ERROR,"【KaTool::ExpDateUtil】 getDate =>  请传入一个正确的Corn");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
        Date parse = simpleDateFormat.parse(corn);
        return parse;
    }
}
