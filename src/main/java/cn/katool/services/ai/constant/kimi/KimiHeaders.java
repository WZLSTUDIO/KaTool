package cn.katool.services.ai.constant.kimi;
public interface KimiHeaders {
    /**
     * Headers 名称	Headers 说明
     * Msh-Context-Cache-Id	当前请求所使用的缓存 id
     * Msh-Context-Cache-Token-Saved	当前请求由于使用了缓存所节省的 Tokens 数量
     * Msh-Context-Cache-Token-Exp	当前缓存的过期时间，即 expired_at
     */
    String Msh_Context_Cache_Id = "Msh-Context-Cache-Id";
    String Msh_Context_Cache_Token_Saved = "Msh-Context-Cache-Token-Saved";
    String Msh_Context_Cache_Token_Exp = "Msh-Context-Cache-Token-Exp";
}
