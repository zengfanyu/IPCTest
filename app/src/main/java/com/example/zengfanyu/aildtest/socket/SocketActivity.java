package com.example.zengfanyu.aildtest.socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zengfanyu.aildtest.R;
import com.example.zengfanyu.aildtest.utils.Constants;
import com.example.zengfanyu.aildtest.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description: 基于Socket的跨进程通信的客户端
 */
public class SocketActivity extends Activity {
    Button mSend;
    EditText mETClientMsg;
    TextView mTVServerRespMsg;
    String mMsgSend;
    PrintWriter mPrintWriter = null;
    Socket mClientSocket = null;

    @SuppressLint("HandlerLeak")
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_SOCKET_CONNECTED:
                    Log.i(Utils.TAG, "connected server socket succeed,port: 23456");
                    Toast.makeText(SocketActivity.this, "socket connected", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_SOCKET_RECEIVE_NEW_MSG:
                    mTVServerRespMsg.setText((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        mSend = findViewById(R.id.id_btn_send_msg);
        mETClientMsg = findViewById(R.id.id_et_client_send);
        mTVServerRespMsg = findViewById(R.id.id_tv_server_resp);


        Intent mIntent = new Intent(SocketActivity.this, SocketServer.class);
        startService(mIntent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectTCPServer();

            }
        }).start();

        mETClientMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mMsgSend = s.toString();
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mMsgSend) && mPrintWriter != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mPrintWriter.println(mMsgSend);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mETClientMsg.setText("");

                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    private void connectTCPServer() {
        Socket socket = null;

        while (socket == null) {
            try {
                //初始化客户端的 Socket
                socket = new Socket("localhost", 23456);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mMainHandler.sendEmptyMessage(Constants.MESSAGE_SOCKET_CONNECTED);
            } catch (IOException e) {
                SystemClock.sleep(1000);
                Log.e(Utils.TAG, "connect tcp server failed ,retry....");
            }
        }

        //接受服务器端的消息
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!SocketActivity.this.isFinishing()) {
                String msg = in.readLine();
                Log.d(Utils.TAG, "client receive msg: " + msg);
                if (msg != null) {
                    Message message = Message.obtain();
                    message.what = Constants.MESSAGE_SOCKET_RECEIVE_NEW_MSG;
                    message.obj = msg;
                    mMainHandler.sendMessage(message);
                }
            }
            Log.d(Utils.TAG, "client quit");
            if (mPrintWriter != null) {
                mPrintWriter.close();
            }
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
