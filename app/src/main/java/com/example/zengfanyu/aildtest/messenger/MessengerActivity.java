package com.example.zengfanyu.aildtest.messenger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.zengfanyu.aildtest.R;
import com.example.zengfanyu.aildtest.utils.Constants;
import com.example.zengfanyu.aildtest.utils.Utils;

/**
 * @author: zengfanyu
 * @Data: 2018/9/4
 * @Description: 运行在独立进程中的客户端
 */
public class MessengerActivity extends Activity {
    //客户端用于和服务端通信的 Messenger，在服务端中定义
    private Messenger mServiceMessenger;
    //服务端用于响应客户端的 Messenger，在客户端中定义
    private Messenger mClientMessenger = new Messenger(new MessengerHandler());

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_SERVICE:
                    Log.d(Utils.TAG, "receive msg from server: " + msg.getData().getString("reply"));
                    break;
                default:
                    break;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Utils.TAG, "onServiceConnected");
            Log.d(Utils.TAG, "onServiceConnected messenger binder: " + service);

            mServiceMessenger = new Messenger(service);
            Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString("msg", "hello this is client");
            msg.setData(data);

            //把接收服务端回复的 Messenger 通过 Message 的 reply 参数传递给服务器
            msg.replyTo = mClientMessenger;
            try {
                //跨进程发送消息
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        final Intent intent = new Intent(this, MessengerService.class);
        findViewById(R.id.id_btn_bind_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindService(intent, mConnection, BIND_AUTO_CREATE);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
