package com.example.zengfanyu.aildtest.binderpool;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description:
 */
public class BinderPoolService extends Service {
    private Binder mBinderPool = new BinderPool.BinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderPool;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
