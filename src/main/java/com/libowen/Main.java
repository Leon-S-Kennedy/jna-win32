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

    public static void main(String[] args) {
        BaseConfig baseConfig = ConfigUtil.getConfig();
        String lpClassName = baseConfig.getLpClassName();
        String lpWindowName = baseConfig.getLpWindowName();
        Map<String, BasePointer> basePointerMap = baseConfig.getBasePointerMap();
        BasePointer valueBasePointer = basePointerMap.get("value");

        WinNT.HANDLE handle = CoreUtil.getProcessHandle(lpClassName,lpWindowName);

        Memory buffer = new Memory(4);
        buffer.setInt(0,5000);
        CoreUtil.writeMemoryByPointer(handle,valueBasePointer,buffer);

        ValueLockTask valueLockTask = CoreUtil.valueLock(handle, valueBasePointer, new Memory(4), 200L);
        try {
            Thread.sleep(36000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        valueLockTask.stop();
        ExecutorService threadPool = CoreUtil.getThreadPool();
        threadPool.shutdown();
    }

    public static void mainx(String[] args) throws IOException {
        BaseConfig baseConfig = ConfigUtil.getConfig();
        String lpClassName = baseConfig.getLpClassName();
        String lpWindowName = baseConfig.getLpWindowName();
        Map<String, CodeInjection> codeInjectionMap = baseConfig.getCodeInjectionMap();
        CodeInjection bits32 = codeInjectionMap.get("32bits");
        WinNT.HANDLE handle = CoreUtil.getProcessHandle(lpClassName,lpWindowName);

        Pointer allocMemory = CoreUtil.codeInjection(handle, bits32);

        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CoreUtil.codeReset(handle,bits32,allocMemory);
    }
}
