package com.example.zengfanyu.aildtest.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zengfanyu.aildtest.Book;
import com.example.zengfanyu.aildtest.IBookManager;
import com.example.zengfanyu.aildtest.IOnNewBookArrivedListener;
import com.example.zengfanyu.aildtest.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: zengfanyu
 * @Data: 2018/9/4
 * @Description: 运行在独立进程中的服务端，用于处理客户端的连接请求
 */
public class AIDLService extends Service {
    /**
     * AIDL 支持的是 List ， 虽然服务端返回的是 CopyOnWriteArrayList 但是在 Binder 中会按照 List 的规范去访问数据并最终形成
     * ArrayList 传递给客户端
     */
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    /**
     * 存放客户端的监听器 {@link RemoteCallbackList}
     */
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();
    /**
     * 用于标记当前 Service 是否销毁
     */
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    /**
     * 需要返回给客户端使用的 Binder
     */
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);

            final int listenerCount = mListenerList.beginBroadcast();
            Log.i(Utils.TAG, "current listener size: " + listenerCount);
            mListenerList.finishBroadcast();
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);

            final int listenerCount = mListenerList.beginBroadcast();
            Log.i(Utils.TAG, "current listener size: " + listenerCount);
            mListenerList.finishBroadcast();

        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(0, "Java"));
        mBookList.add(new Book(1, "Android"));
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            while (!mIsServiceDestroyed.get()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size();
                Book newBook = new Book(bookId, "NewBook#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        int n = mListenerList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            if (listener != null) {
                //远程服务端调用客户端额的方法，此方法运行在客户端Binder的线程池里，若此方法耗时，那么需要确保
                //这里的 onNewBookArrived 方法运行在子线程中，否则会导致服务端无法响应
                listener.onNewBookArrived(book);
            }
        }
        mListenerList.finishBroadcast();
    }
}
