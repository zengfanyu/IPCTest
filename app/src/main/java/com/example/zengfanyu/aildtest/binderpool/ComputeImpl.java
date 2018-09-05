package com.example.zengfanyu.aildtest.binderpool;

import android.os.RemoteException;

import com.example.zengfanyu.aildtest.ICompute;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description:
 */
public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
