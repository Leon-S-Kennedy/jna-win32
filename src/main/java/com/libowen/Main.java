package com.libowen;

import com.libowen.model.BaseConfig;
import com.libowen.model.BasePointer;
import com.libowen.model.CodeInjection;
import com.libowen.task.ValueLockTask;
import com.libowen.utils.ConfigUtil;
import com.libowen.utils.CoreUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: libw1
 * @Date: 2024/05/28/15:25
 * @Description:
 */
public class Main {

    public static void mainx(String[] args) {
        BaseConfig baseConfig = ConfigUtil.getConfig();
        Map<String, BasePointer> basePointerMap = baseConfig.getBasePointerMap();
        BasePointer valueBasePointer = basePointerMap.get("value");

        WinNT.HANDLE handle = CoreUtil.getProcessHandle(baseConfig);

        Memory buffer = new Memory(8);
        //CoreUtil.readMemoryByPointer(handle,valueBasePointer,buffer);

//        System.out.println(buffer.getInt(0));

        Pointer tempAddress = CoreUtil.calcAddress(handle, valueBasePointer, buffer);
        buffer.setInt(0,5000);
        ValueLockTask valueLockTask = new ValueLockTask(handle, tempAddress, buffer, 200);
        ExecutorService threadPool = CoreUtil.getThreadPool();
        threadPool.submit(valueLockTask);
        try {
            Thread.sleep(3600000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        BaseConfig baseConfig = ConfigUtil.getConfig();
        Map<String, CodeInjection> codeInjectionMap = baseConfig.getCodeInjectionMap();
        CodeInjection bits32 = codeInjectionMap.get("32bits");
        WinNT.HANDLE handle = CoreUtil.getProcessHandle(baseConfig);

        Pointer allocMemory = CoreUtil.codeInjection(handle, bits32);

        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CoreUtil.codeReset(handle,bits32,allocMemory);
    }
}
