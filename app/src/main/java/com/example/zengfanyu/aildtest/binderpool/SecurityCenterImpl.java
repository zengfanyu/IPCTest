package com.example.zengfanyu.aildtest.binderpool;

import android.os.RemoteException;

import com.example.zengfanyu.aildtest.ISecurityCenter;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description:
 */
public class SecurityCenterImpl extends ISecurityCenter.Stub {
    private static final char SECRET_CODE = '^';

    @Override
    public String encrypy(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypy(password);
    }
}
