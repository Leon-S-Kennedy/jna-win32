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
    private List<String> jumpCodeList;
    private List<String> originalCodeList;
    private List<String> newCodeList;

    public BasePointer getBasePointer() {
        return basePointer;
    }

    public void setBasePointer(BasePointer basePointer) {
        this.basePointer = basePointer;
    }

    public List<String> getJumpCodeList() {
        return jumpCodeList;
    }
    public void setJumpCodeList(List<String> jumpCodeList) {
        this.jumpCodeList = jumpCodeList;
    }

    public byte [] getJumpCodeByteArray(){
        Byte[] byteArray = jumpCodeList.stream()
                .map(s -> Integer.parseInt(s, 16))
                .map(Integer::byteValue)
                .toArray(Byte[]::new);
        byte [] bytes=new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            bytes[i]=byteArray[i];
        }
        return bytes;
    }

    public Memory getJumpCodeMemory(){
        byte[] jumpCodeByteArray = getJumpCodeByteArray();
        int length = jumpCodeByteArray.length;
        Memory buffer = new Memory(length);
        buffer.write(0,jumpCodeByteArray,0,length);
        return buffer;
    }

    public List<String> getOriginalCodeList() {
        return originalCodeList;
    }

    public void setOriginalCodeList(List<String> originalCodeList) {
        this.originalCodeList = originalCodeList;
    }

    public byte [] getOriginalCodeByteArray(){
        Byte[] byteArray = originalCodeList.stream()
                .map(s -> Integer.parseInt(s, 16))
                .map(Integer::byteValue)
                .toArray(Byte[]::new);
        byte [] bytes=new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            bytes[i]=byteArray[i];
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


    public List<String> getNewCodeList() {
        return newCodeList;
    }


    public void setNewCodeList(List<String> newCodeList) {
        this.newCodeList = newCodeList;
    }

    public byte [] getNewCodeByteArray(){
        Byte[] byteArray = newCodeList.stream()
                .map(s -> Integer.parseInt(s, 16))
                .map(Integer::byteValue)
                .toArray(Byte[]::new);
        byte [] bytes=new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            bytes[i]=byteArray[i];
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
