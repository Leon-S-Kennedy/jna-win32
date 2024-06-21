package com.libowen.utils;

import com.libowen.model.BaseConfig;
import com.libowen.model.BasePointer;
import com.libowen.model.CodeInjection;
import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: libw1
 * @Date: 2024/06/15/16:14
 * @Description:
 */
public class CoreUtil {

    private static int SYSTEM_BITS;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(16);

    public final static User32 user32 = User32.INSTANCE;

    public final static Kernel32 kernel32 = Kernel32.INSTANCE;

    public static ExecutorService getThreadPool(){
        return executorService;
    }

    static{
        if (!Platform.isWindows()) {
            SYSTEM_BITS=Platform.is64Bit()?64:32;
        }
        IntByReference isWow64 = new IntByReference(0);
        WinNT.HANDLE handle = kernel32.GetCurrentProcess();
        if (handle == null) {
            throw new RuntimeException("获取当前进程的进程句柄失败！");
        }
        boolean result = kernel32.IsWow64Process(handle, isWow64);
        if(!result){
            throw new RuntimeException("执行IsWow64Process失败");
        }
        SYSTEM_BITS=isWow64.getValue()==0?64:32;
    }

    public static WinNT.HANDLE getProcessHandleByPid(int pid){
        WinNT.HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, pid);
        if(handle==null){
            throw new RuntimeException("获取进程句柄失败！code: "+(kernel32.GetLastError()));
        }
        return handle;
    }


    public static WinNT.HANDLE getProcessHandle(BaseConfig baseConfig){
        WinDef.HWND windowHandle = getWindowHandle(baseConfig.getLpClassName(), baseConfig.getLpWindowName());
        int pid = getPidByWindowHandle(windowHandle);
        WinNT.HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, pid);
        if(handle==null){
            throw new RuntimeException("获取进程句柄失败！code: "+(kernel32.GetLastError()));
        }
        return handle;
    }
    public static WinDef.HWND getWindowHandle(String lpClassName, String lpWindowName){
        WinDef.HWND hwnd = user32.FindWindow(lpClassName, lpWindowName);
        if (hwnd==null){
            throw new RuntimeException("获取窗口句柄失败！");
        }
        return hwnd;
    }

    public static int getPidByWindowHandle(WinDef.HWND windowHandle){
        IntByReference intByReference = new IntByReference();
        user32.GetWindowThreadProcessId(windowHandle, intByReference);
        int pid = intByReference.getValue();
        if (pid==0){
            throw new RuntimeException("通过窗口句柄获取pid失败！");
        }
        return pid;
    }

    public static void readProcessMemoryByAddress(WinNT.HANDLE handle, Pointer address, Memory buffer){
        if (!kernel32.ReadProcessMemory(handle,address,buffer,(int)buffer.size(),null)) {
            int lastError = kernel32.GetLastError();
            kernel32.CloseHandle(handle);
            throw new RuntimeException("读取进程内存失败！code: "+ lastError);
        }
    }

    public static void writeProcessMemoryByAddress(WinNT.HANDLE handle, Pointer address, Memory buffer){
        if (!kernel32.WriteProcessMemory(handle,address,buffer,(int)buffer.size(),null)) {
            int lastError = kernel32.GetLastError();
            kernel32.CloseHandle(handle);
            throw new RuntimeException("写入进程内存失败！code: "+ lastError);
        }
    }

    public static void readMemoryByPointer(WinNT.HANDLE handle, BasePointer basePointer, Memory buffer) {
        Pointer address = calcAddress(handle, basePointer, buffer);
        readProcessMemoryByAddress(handle,address,buffer);
    }

    public static void writeMemoryByPointer(WinNT.HANDLE handle, BasePointer basePointer, Memory buffer){
        Memory readBuffer = new Memory(8);
        Pointer pointer = calcAddress(handle, basePointer, readBuffer);
        writeProcessMemoryByAddress(handle,pointer,buffer);
    }

    public static Pointer calcAddress(WinNT.HANDLE handle, BasePointer basePointer, Memory buffer){
        long addressValue = basePointer.getAddressValue();
        List<Integer> offsetValueList = basePointer.getOffsetValueList();
        Pointer result = null;
        if(offsetValueList==null|| offsetValueList.isEmpty()){
            result = new Pointer(addressValue);
        }else {
            readProcessMemoryByAddress(handle,new Pointer(addressValue),buffer);
            for (int index = 0; index < offsetValueList.size(); index++) {
                long address = buffer.getLong(0);
                long offset = (long)offsetValueList.get(index);
                Pointer pointer = new Pointer(address+offset);
                if(index<offsetValueList.size()-1){
                    readProcessMemoryByAddress(handle,pointer,buffer);
                }else {
                    result = pointer;
                }
            }
        }
        return result;
    }

    public static Pointer getVirtualAllocMemory(WinNT.HANDLE handle,long allocSize){
        Pointer pointer = kernel32.VirtualAllocEx(handle, null, new BaseTSD.SIZE_T(allocSize), WinNT.MEM_COMMIT, WinNT.PAGE_EXECUTE_READWRITE);
        if(pointer==null){
            throw new RuntimeException("申请新的内存失败！");
        }
        return pointer;
    }

    private static void printByteList(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入字节集:");
        String line = scanner.nextLine();
        String[] strings = line.split("\\s+");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        int length = strings.length;
        for (int i = 0; i < length; i++) {
            if(i<length-1){
                stringBuilder.append("\"").append(strings[i]).append("\"").append(",");
            }else {
                stringBuilder.append("\"").append(strings[i]).append("\"");
            }
        }
        stringBuilder.append("]");
        System.out.println(stringBuilder);
    }

    public static boolean is64BitsProcess(WinNT.HANDLE handle){
        if(SYSTEM_BITS!=64){
            return false;
        }
        IntByReference wow64Process = new IntByReference(0);
        boolean result = kernel32.IsWow64Process(handle, wow64Process);
        if(!result){
            throw new RuntimeException("执行IsWow64Process失败");
        }
        return wow64Process.getValue()==0;
    }

    public static void test(String[] args){
//        Pointer virtualAllocEx = kernel32.VirtualAllocEx(new WinNT.HANDLE(), null, new BaseTSD.SIZE_T(4 * 1024), WinNT.MEM_COMMIT, WinNT.PAGE_EXECUTE_READWRITE);

    }

    public static void codeInjection(WinNT.HANDLE handle,CodeInjection coolDownTime) {
        byte[] originalCodeByteArray = coolDownTime.getOriginalCodeByteArray();
        byte[] newCodeByteArray = coolDownTime.getNewCodeByteArray();
        int originalLength = originalCodeByteArray.length;
        if(originalLength == newCodeByteArray.length){
            //原始代码和注入代码的字节数一样，不用额外分配内存
            writeMemoryByPointer(handle,coolDownTime.getBasePointer(),coolDownTime.getNewCodeMemory());
        }else {
            if(!is64BitsProcess(handle)){
                //32位程序的注入
                if(originalLength<8){
                    throw new RuntimeException("originalCode的字节长度有误,不支持跳转！");
                }
                Pointer allocMemory = getVirtualAllocMemory(handle, 1024);
                byte[] bytes = new byte[originalLength];
                bytes[0]=(byte) 0x50;
                bytes[1]=(byte) 0xB8;
                int fourByteNumber = 0x12345678;
                bytes[2] = (byte) (fourByteNumber & 0xFF);
                bytes[3] = (byte) ((fourByteNumber >> 8) & 0xFF);
                bytes[4] = (byte) ((fourByteNumber >> 16) & 0xFF);
                bytes[5] = (byte) ((fourByteNumber >> 24) & 0xFF);
                bytes[6]=(byte) 0xFF;
                bytes[7]=(byte) 0xE0;
                for (int i = 8; i < originalLength; i++) {
                    bytes[i] = (byte) 0x90;
                }
            }else {
                //64位程序的注入
            }
        }
    }

    public static void main(String[] args) {
//        int originalLength = 20;
//        byte[] bytes = new byte[originalLength];
//        bytes[0]=(byte) 0x50;
//        bytes[1]=(byte) 0xB8;
//        int fourByteNumber = 0x12345678;
//        bytes[2] = (byte) (fourByteNumber & 0xFF);
//        bytes[3] = (byte) ((fourByteNumber >> 8) & 0xFF);
//        bytes[4] = (byte) ((fourByteNumber >> 16) & 0xFF);
//        bytes[5] = (byte) ((fourByteNumber >> 24) & 0xFF);
//        bytes[6]=(byte) 0xFF;
//        bytes[7]=(byte) 0xE0;
//        for (int i = 8; i < originalLength; i++) {
//            bytes[i] = (byte) 0x90;
//        }
//        for (byte b : bytes) {
//            System.out.printf("%02X ", b);
//        }


        // 也可以使用 Java 内置的 ByteOrder 来检查字节序
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            System.out.println("使用 ByteOrder：当前系统是大端字节序 (Big-endian)");
        } else if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            System.out.println("使用 ByteOrder：当前系统是小端字节序 (Little-endian)");
        }
    }
}
