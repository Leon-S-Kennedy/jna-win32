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
import java.util.Arrays;
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

    public static void freeVirtualAllocMemory(WinNT.HANDLE handle, Pointer allocMemory){
        boolean result = kernel32.VirtualFreeEx(handle, allocMemory, new BaseTSD.SIZE_T(0), Kernel32.MEM_RELEASE);
        if (!result) {
            System.err.println("内存释放失败");
        }
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
    private static void printHexArray(byte[] bytes){
        String[] strings = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            strings[i]=String.format("%02X", bytes[i]);
        }
        System.out.println(Arrays.toString(strings));
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

    public static Pointer codeInjection(WinNT.HANDLE handle,CodeInjection injectionObject) {
        byte[] originalCodeByteArray = injectionObject.getOriginalCodeByteArray();
        byte[] newCodeByteArray = injectionObject.getNewCodeByteArray();
        BasePointer basePointer = injectionObject.getBasePointer();
        Memory newCodeMemory = injectionObject.getNewCodeMemory();
        int originalLength = originalCodeByteArray.length;
        Pointer allocMemory = null;
        if(originalLength == newCodeByteArray.length){
            writeMemoryByPointer(handle, basePointer, newCodeMemory);
        }else {
            Memory memory = new Memory(originalLength);

            if(!is64BitsProcess(handle)){
                //32位程序的注入
                if(originalLength<9){
                    throw new RuntimeException("32bits下的originalCode的字节长度有误,不支持跳转！");
                }
                allocMemory = getVirtualAllocMemory(handle, 1024);

                memory.setByte(0,(byte) 0x50);
                memory.setByte(1,(byte) 0xB8);
                memory.setInt(2,(int) Pointer.nativeValue(allocMemory));
                memory.setByte(6,(byte) 0xFF);
                memory.setByte(7,(byte) 0xE0);
                memory.setByte(8,(byte) 0x58);
                for (int i = 9; i < originalLength; i++) {
                    memory.setByte(i,(byte) 0x90);
                }
            }else {
                //64位程序的注入
                if(originalLength<14){
                    throw new RuntimeException("64位下的originalCode的字节长度有误,不支持跳转！");
                }
                allocMemory = getVirtualAllocMemory(handle, 1024);
                memory.setByte(0,(byte) 0x50);
                memory.setByte(1,(byte) 0x48);
                memory.setByte(2,(byte) 0xB8);
                memory.setLong(3,Pointer.nativeValue(allocMemory));
                memory.setByte(11,(byte) 0xFF);
                memory.setByte(12,(byte) 0xE0);
                memory.setByte(13,(byte) 0x58);
                for (int i = 14; i < originalLength; i++) {
                    memory.setByte(i,(byte) 0x90);
                }
            }
            writeMemoryByPointer(handle, basePointer,memory);
            writeProcessMemoryByAddress(handle,allocMemory,newCodeMemory);
        }
        return allocMemory;
    }
    public static void codeReset(WinNT.HANDLE handle, CodeInjection injectionObject,Pointer allocMemory) {
        BasePointer basePointer = injectionObject.getBasePointer();
        Memory originalCodeMemory = injectionObject.getOriginalCodeMemory();

        writeMemoryByPointer(handle, basePointer, originalCodeMemory);
        if(allocMemory!=null){
            //说明此处分配了内存需要释放
            freeVirtualAllocMemory(handle,allocMemory);
        }
    }
}
