package com.libowen.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: libw1
 * @Date: 2024/06/15/14:17
 * @Description:
 */
public class BasePointer {

    private String address;

    private List<String> offsetList;


    public String getAddress() {
        return address;
    }

    public long getAddressValue() {
        return Long.parseLong(address,16);
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public List<String> getOffsetList() {
        return offsetList;
    }

    public List<Integer> getOffsetValueList(){
        List<Integer> list = offsetList.stream().map(s -> Integer.parseInt(s, 16)).collect(Collectors.toList());
        return  list;
    }

    public void setOffsetList(List<String> offsetList) {
        this.offsetList = offsetList;
    }

}
