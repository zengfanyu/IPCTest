package com.example.zengfanyu.aildtest.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zengfanyu.aildtest.utils.Constants;
import com.example.zengfanyu.aildtest.utils.Utils;

/**
 * @author: zengfanyu
 * @Data: 2018/9/4
 * @Description: 运行在独立进程中的服务端，用于处理客户端的连接请求
 */
public class MessengerService extends Service {
    private final Messenger mServiceMessenger = new Messenger(new MessengerHandler());

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_CLIENT:
                    Log.d(Utils.TAG, "receive msg from client: " + msg.getData().getString("msg"));
                    //取出客户端的 Messenger
                    Messenger clientMessenger = msg.replyTo;

                    Message replyMsg = Message.obtain(null, Constants.MSG_FROM_SERVICE);
                    Bundle data = new Bundle();
                    data.putString("reply", "hello client this is server");
                    replyMsg.setData(data);

                    try {
                        //服务端返回消息给客户端
                        clientMessenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //返回 Messenger 底层的 Binder
        return mServiceMessenger.getBinder();
    }
}
