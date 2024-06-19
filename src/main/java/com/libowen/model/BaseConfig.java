package com.libowen.model;

import java.util.Map;

/**
 * @Author: libw1
 * @Date: 2024/06/15/14:55
 * @Description:
 */
public class BaseConfig {

    private String lpClassName;

    private String lpWindowName;

    private Map<String,BasePointer> basePointerMap;

    private Map<String,CodeInjection> codeInjectionMap;


    public String getLpClassName() {
        return lpClassName;
    }

    public void setLpClassName(String lpClassName) {
        this.lpClassName = lpClassName;
    }

    public String getLpWindowName() {
        return lpWindowName;
    }

    public void setLpWindowName(String lpWindowName) {
        this.lpWindowName = lpWindowName;
    }

    public Map<String, BasePointer> getBasePointerMap() {
        return basePointerMap;
    }

    public void setBasePointerMap(Map<String, BasePointer> basePointerMap) {
        this.basePointerMap = basePointerMap;
    }

    public Map<String, CodeInjection> getCodeInjectionMap() {
        return codeInjectionMap;
    }

    public void setCodeInjectionMap(Map<String, CodeInjection> codeInjectionMap) {
        this.codeInjectionMap = codeInjectionMap;
    }
}
