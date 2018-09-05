package com.example.zengfanyu.aildtest.binderpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.zengfanyu.aildtest.IBinderPool;

import java.net.ContentHandler;
import java.util.concurrent.CountDownLatch;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description: Binder 连接池的具体实现，用于绑定远程服务并返回特定Binder给客户端
 */
public class BinderPool {
    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mAppContext;
    private IBinderPool mIBinderPool;
    private static volatile BinderPool sBinderPool;
    private CountDownLatch mCountDownLatch;

    private BinderPool(Context context) {
        mAppContext = context.getApplicationContext();
        connectBinderPoolService();

    }

    public static BinderPool getInstance(Context context) {
        if (sBinderPool == null) {
            synchronized (BinderPool.class) {//这里的锁是类锁
                if (sBinderPool == null) {
                    sBinderPool = new BinderPool(context);
                }
            }
        }
        return sBinderPool;
    }

    private synchronized void connectBinderPoolService() {//这里的的 synchronized 用到的锁是对象锁
        mCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(mAppContext, BinderPoolService.class);
        mAppContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            // TODO: 2018/9/5 处理中断
            e.printStackTrace();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBinderPool = IBinderPool.Stub.asInterface(service);

            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIBinderPool = null;
            connectBinderPoolService();
        }
    };

    /**
     * 通过 binderCode 从 BinderPool 中查询对应的 Binder
     *
     * @param binderCode 某一个 Binder 的标记
     * @return 标记为 binderCode 的 Binder
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        if (mIBinderPool != null) {
            try {
                binder = mIBinderPool.queryBinder(binderCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return binder;
    }

    public static class BinderPoolImpl extends IBinderPool.Stub {
        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder resultBinder = null;
            switch (binderCode) {
                case BINDER_SECURITY_CENTER:
                    resultBinder = new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    resultBinder = new ComputeImpl();
                    break;
                default:
                    break;
            }
            return resultBinder;
        }
    }
}
