package com.libowen.model;

import com.sun.jna.Memory;

import java.util.List;

/**
 * @Author: libw1
 * @Date: 2024/06/18/9:45
 * @Description:
 */
public class CodeInjection {

    private BasePointer basePointer;
    //private List<String> jumpCodeList;
    private String originalCode;
    private String newCode;

    public BasePointer getBasePointer() {
        return basePointer;
    }

    public void setBasePointer(BasePointer basePointer) {
        this.basePointer = basePointer;
    }


    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public byte [] getOriginalCodeByteArray(){
        String[] split = originalCode.split("\\s+");
        byte [] bytes=new byte[split.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i]= (byte) Integer.parseInt(split[i], 16);
        }
        return bytes;
    }

    public Memory getOriginalCodeMemory(){
        byte[] originalCodeByteArray = getOriginalCodeByteArray();
        int length = originalCodeByteArray.length;
        Memory buffer = new Memory(length);
        buffer.write(0,originalCodeByteArray,0,length);
        return buffer;
    }

    public String getNewCode() {
        return newCode;
    }


    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }

    public byte [] getNewCodeByteArray(){
        String[] split = newCode.split("\\s+");
        byte [] bytes=new byte[split.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i]= (byte) Integer.parseInt(split[i], 16);
        }
        return bytes;
    }

    public Memory getNewCodeMemory(){
        byte[] newCodeByteArray = getNewCodeByteArray();
        int length = newCodeByteArray.length;
        Memory buffer = new Memory(length);
        buffer.write(0,newCodeByteArray,0,length);
        return buffer;
    }


}
