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

//    public static void main1(String[] args) throws Exception {
//        //读取基本配置
//        BaseConfig baseConfig = ConfigUtil.getConfig();
//        //获取进程句柄
//        WinNT.HANDLE handle = CoreUtil.getProcessHandle(baseConfig);
//
//        //获取基指针对象
//        BasePointer basePointer = baseConfig.getBasePointer();
//
//        Memory buffer = new Memory(8);
//        CoreUtil.readMemoryByPointer(handle, basePointer,buffer);
//
//        System.out.println(buffer.getInt(0));
//
//
//        int newValue = 5000;
//        buffer.setInt(0, newValue);
//        while (true){
//            CoreUtil.writeMemoryByPointer(handle, basePointer,buffer);
//
//        }
//
//        //CoreUtil.kernel32.CloseHandle(handle);
//    }

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
        CodeInjection coolDownTime = codeInjectionMap.get("coolDownTime");
        WinNT.HANDLE handle = CoreUtil.getProcessHandle(baseConfig);

        Pointer allocPointer = CoreUtil.getVirtualAllocMemory(handle,1024);

        CoreUtil.codeInjection(handle,coolDownTime);
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //CoreUtil.codeReset(handle,coolDownTime);

        long newMemoryAddress = Pointer.nativeValue(allocPointer);


        long l2 = Pointer.nativeValue(CoreUtil.calcAddress(handle, coolDownTime.getBasePointer(), new Memory(8)));
        long l = newMemoryAddress - l2 - 5;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeLong(l);
        byte[] bytes = byteArrayOutputStream.toByteArray();


        //CoreUtil.writeMemoryByPointer(handle, coolDownTime.getBasePointer(), );
//        CoreUtil.writeMemoryByPointer(handle, coolDownTime.getBasePointer(), coolDownTime.getNewCodeMemory());
//
//        try {
//            TimeUnit.SECONDS.sleep(60);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        CoreUtil.writeMemoryByPointer(handle, coolDownTime.getBasePointer(), coolDownTime.getOriginalCodeMemory());
//
//        //handle.
//
//        Memory originalCodeBuffer = coolDownTime.getOriginalCodeMemory();
        //CoreUtil.writeMemoryByPointer(new WinNT.HANDLE(), coolDownTime.getBasePointer(),originalCodeBuffer);
    }
}
