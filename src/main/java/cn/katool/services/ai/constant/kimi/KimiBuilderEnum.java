package cn.katool.services.ai.constant.kimi;

import java.util.Arrays;
import java.util.List;

public enum KimiBuilderEnum {

    BEGIN("begin"),
    BASE("base",Arrays.asList(BEGIN)),
    CHAT("chat",Arrays.asList(BASE)),
    COMPLETIONS("completions",Arrays.asList(CHAT)),
    FILES("files",Arrays.asList(BASE)),
    TOKENIZERS("tokenizers",Arrays.asList(BASE)),
    ESTIMATE_TOKEN_COUNT("estimate-token-count",Arrays.asList(TOKENIZERS)),

    USERS("users",Arrays.asList(BASE)),
    ME("me",Arrays.asList(USERS)),
    BALANCE("balance",Arrays.asList(ME)),
    CACHING("caching",Arrays.asList(BASE)),
    REFS("refs",Arrays.asList(CACHING)),
    TAGS("tags",Arrays.asList(REFS)),
    CONTENT("content",Arrays.asList(FILES,TAGS)),
    END("end");
    private String name;
    private List<KimiBuilderEnum> lastStatusList;

    public List<KimiBuilderEnum> getLastStatus() {
        return lastStatusList;
    }

    public void setLastStatus(List<KimiBuilderEnum> lastStatus) {
        this.lastStatusList = lastStatus;
    }

    KimiBuilderEnum(List<KimiBuilderEnum> lastStatus) {
        this.lastStatusList = lastStatus;
    }

    KimiBuilderEnum(String name,List<KimiBuilderEnum> lastStatus) {
        this.name = name;
        this.lastStatusList = lastStatus;
    }

    KimiBuilderEnum(String name) {
        this.name = name;
        if (this.name == "end"){
            this.lastStatusList = Arrays.asList(this);
        }
    }

    KimiBuilderEnum() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
