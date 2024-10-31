package cn.katool.util.remote.formatChain;

public interface EnDeCodeFormat {
    boolean validEncode(Object obj);
    boolean validDecode(Object encode);
    Object encode(Object obj) throws Exception;
    Object decode(Object encode, Object backDao) throws Exception;
    EnDeCodeFormat then(EnDeCodeFormat next);
    Object systemEncode(Object obj) throws Exception;
    Object systemDecode(Object encode, Object backDao) throws Exception;
}
