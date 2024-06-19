package com.libowen.task;

import com.libowen.utils.CoreUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: libw1
 * @Date: 2024/06/17/15:04
 * @Description:
 */
public class ValueLockTask implements Runnable {

    private volatile boolean lock = true;

    private final WinNT.HANDLE handle;

    private final Pointer address;

    private final Memory valueLockBuffer;

    private long interval;

    public ValueLockTask(WinNT.HANDLE handle, Pointer address, Memory valueLockBuffer,long interval) {
        this.handle = handle;
        this.address = address;
        this.valueLockBuffer = valueLockBuffer;
        this.interval = interval;
    }




    @Override
    public void run() {
        byte[] valueLockByteArray = valueLockBuffer.getByteArray(0, (int) valueLockBuffer.size());
        Memory buffer = new Memory(valueLockBuffer.size());
        while (lock){
            CoreUtil.readProcessMemoryByAddress(handle,address,buffer);
            byte[] byteArray = buffer.getByteArray(0, (int) buffer.size());
            if(!Arrays.equals(valueLockByteArray,byteArray)){
                CoreUtil.writeProcessMemoryByAddress(handle,address,valueLockBuffer);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        lock = false;
    }

}
