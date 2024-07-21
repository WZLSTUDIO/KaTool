package cn.katool.services.ai.model.dto.kimi.cache;

import cn.katool.services.ai.model.dto.kimi.base.KimiBaseRequest;

import java.util.HashMap;

public class KimiCacheRequest extends KimiBaseRequest {
    String name;
    String description;
    HashMap<String,String> metadata;
    Integer expire_at;
    Integer ttl;
}
