package cn.katool.services.ai.constant;

import java.util.Arrays;
import java.util.List;

public enum KimiBuilderEnum {

    BEGIN("begin"),
    BASE("base",Arrays.asList(BEGIN)),
    CHAT("chat",Arrays.asList(BASE)),
    COMPLETIONS("Completions",Arrays.asList(CHAT)),
    FILES("files",Arrays.asList(BASE)),
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
