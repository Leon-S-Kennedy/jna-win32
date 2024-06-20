package com.libowen.utils;

import com.libowen.model.BaseConfig;
import com.libowen.model.BasePointer;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

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
    private static final ExecutorService executorService = Executors.newFixedThreadPool(16);

    public final static User32 user32 = User32.INSTANCE;

    public final static Kernel32 kernel32 = Kernel32.INSTANCE;

    public static ExecutorService getThreadPool(){
        return executorService;
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

    public static Pointer getVirtualAllocPointer(WinNT.HANDLE handle){

        Pointer pointer = kernel32.VirtualAllocEx(handle, null, new BaseTSD.SIZE_T( 1024), WinNT.MEM_COMMIT, WinNT.PAGE_EXECUTE_READWRITE);
        if (pointer==null){
            throw new RuntimeException("分配新内存失败！");
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

    public static void test(String[] args){
        printByteList();
//        Pointer virtualAllocEx = kernel32.VirtualAllocEx(new WinNT.HANDLE(), null, new BaseTSD.SIZE_T(4 * 1024), WinNT.MEM_COMMIT, WinNT.PAGE_EXECUTE_READWRITE);

    }
}
