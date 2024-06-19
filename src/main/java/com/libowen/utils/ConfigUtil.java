package com.libowen.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.libowen.model.BaseConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @Author: libw1
 * @Date: 2024/06/15/14:16
 * @Description:
 */
public class ConfigUtil {

    public static BaseConfig getConfig(){
        Gson gson = new Gson();

        BaseConfig baseConfig = null;
        try {
            baseConfig = gson.fromJson(new JsonReader(new FileReader("src/main/resources/base-config.json")), BaseConfig.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (baseConfig==null){
            throw new RuntimeException("读取配置文件失败！");
        }
        return baseConfig;
    }

    public static void saveAsJsonFile(BaseConfig baseConfig){
        Gson gson = new Gson();
        String s = gson.toJson(baseConfig);
        System.out.println(s);
    }
}
