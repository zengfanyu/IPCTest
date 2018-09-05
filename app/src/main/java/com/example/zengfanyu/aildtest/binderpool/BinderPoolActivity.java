package com.example.zengfanyu.aildtest.binderpool;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zengfanyu.aildtest.ICompute;
import com.example.zengfanyu.aildtest.ISecurityCenter;
import com.example.zengfanyu.aildtest.R;
import com.example.zengfanyu.aildtest.utils.Utils;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description:
 */
public class BinderPoolActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_pool);

        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    private void doWork() {
        BinderPool binderPool = BinderPool.getInstance(this);

        IBinder securityBinder = binderPool.queryBinder(BinderPool.BINDER_SECURITY_CENTER);
        ISecurityCenter iSecurityCenter = SecurityCenterImpl.asInterface(securityBinder);
        Log.d(Utils.TAG, "visit security center");
        String msg = "hello android";
        Log.d(Utils.TAG, "content: " + msg);
        try {
            String password = iSecurityCenter.encrypy(msg);
            Log.d(Utils.TAG, "encrypt: " + password);
            Log.d(Utils.TAG, "decrypt: " + iSecurityCenter.decrypt(password));
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        IBinder computeBinder = binderPool.queryBinder(BinderPool.BINDER_COMPUTE);
        ICompute iCompute = ComputeImpl.asInterface(computeBinder);
        Log.d(Utils.TAG, "visit compute");

        try {
            Log.d(Utils.TAG, "3+5=" + iCompute.add(3, 5));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
