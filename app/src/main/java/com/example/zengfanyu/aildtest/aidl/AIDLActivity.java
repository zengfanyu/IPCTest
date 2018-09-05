package com.example.zengfanyu.aildtest.aidl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zengfanyu.aildtest.Book;
import com.example.zengfanyu.aildtest.IBookManager;
import com.example.zengfanyu.aildtest.IOnNewBookArrivedListener;
import com.example.zengfanyu.aildtest.MainActivity;
import com.example.zengfanyu.aildtest.R;
import com.example.zengfanyu.aildtest.utils.Constants;
import com.example.zengfanyu.aildtest.utils.Utils;

import java.util.List;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description:
 */
public class AIDLActivity extends Activity {

    @SuppressLint("HandlerLeak")
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i(Utils.TAG, "receive new book: " + msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl);
        Intent intent = new Intent(this,AIDLService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private IBookManager mRemoteBookManager;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Utils.TAG, "onServiceConnected");
            //将服务端的 Binder 对象转换成客户端需要的 AIDL 接口对象
            IBookManager mIBookManager = IBookManager.Stub.asInterface(service);
            mRemoteBookManager = mIBookManager;
            Book book = new Book(2, "iOS");
            try {
                //addBook 是远程Service中的方法，运行在服务端的 Binder 线程池中，同时客户端线程会被挂起
                //如果服务端方法执行比较耗时，那么这里会导致 ANR ，此处为主线程
                mIBookManager.addBook(book);
                List<Book> bookList = mIBookManager.getBookList();
                for (Book book1 : bookList) {
                    Log.i(Utils.TAG, "book id: " + book1.bookId + " book name: " + book1.bookName);
                }
                //注册监听器
                mIBookManager.registerListener(mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Binder 若意外死亡了，在此处可以进程重连
            Log.d(Utils.TAG, "onServiceDisconnected");
            mRemoteBookManager = null;
            Intent intent = new Intent(AIDLActivity.this, AIDLService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    private IOnNewBookArrivedListener mIOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            Log.i(Utils.TAG, "client onNewBookArrived pid: " + Process.myPid() + " tid: " + Process.myTid());
            //在客户端的Binder子线程中调用，所以不能在这里访问UI
            mMainHandler.obtainMessage(Constants.MESSAGE_NEW_BOOK_ARRIVED, newBook.bookName).sendToTarget();
        }
    };

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i(Utils.TAG, "unregister listeners");
                //注销监听器
                mRemoteBookManager.unregisterListener(mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mServiceConnection);
        }
        super.onDestroy();
    }
}
