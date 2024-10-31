package cn.katool.util.remote.formatChain;

import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;

public class DefaultEnDeCodeFormat implements EnDeCodeFormat {
    private EnDeCodeFormat next;

    @Override
    public boolean validEncode(Object obj) {
        return true;
    }

    @Override
    public boolean validDecode(Object encode) {
        return true;
    }

    @Override
    public Object encode(Object obj) throws Exception {
        throw new KaToolException(ErrorCode.PARAMS_ERROR,"Encode method not implemented");
    }

    @Override
    public Object decode(Object encode, Object backDao) throws Exception {
        throw new KaToolException(ErrorCode.PARAMS_ERROR,"Decode method not implemented");
    }

    @Override
    public EnDeCodeFormat then(EnDeCodeFormat next) {
        this.next = next;
        return next;
    }

    @Override
    public Object systemEncode(Object obj) throws Exception {
        if (!validEncode(obj)) {
            throw new KaToolException(ErrorCode.PARAMS_ERROR,"Encoding failed validation");
        }
        Object encoded = encode(obj);
        if (next != null) {
            return next.systemEncode(encoded);
        }
        return encoded;
    }

    @Override
    public Object systemDecode(Object encode, Object backDao) throws Exception {
        if (!validDecode(encode)) {
            throw new KaToolException(ErrorCode.PARAMS_ERROR,"Decoding failed validation");
        }
        Object decoded = decode(encode, backDao);
        if (next != null) {
            return next.systemDecode(decoded, backDao);
        }
        return decoded;
    }
}
