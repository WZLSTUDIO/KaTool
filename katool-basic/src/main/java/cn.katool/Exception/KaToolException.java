/**
 * Title
 *
 * @ClassName: KaToolException
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/1/29 2:20
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.Exception;
public class KaToolException extends RuntimeException{
    private final int code;
    public KaToolException(int code, String message) {
        super(message);
        this.code = code;
    }
    public KaToolException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    public KaToolException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    public int getCode() {
        return code;
    }
}
