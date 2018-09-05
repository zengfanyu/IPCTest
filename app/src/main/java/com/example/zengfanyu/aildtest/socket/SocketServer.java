package com.example.zengfanyu.aildtest.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.zengfanyu.aildtest.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: zengfanyu
 * @Data: 2018/9/5
 * @Description: 基于Socket的跨进程通信的服务端
 */
public class SocketServer extends Service {
    private AtomicBoolean isServiceDestroy = new AtomicBoolean(false);
    private String[] mDefinedMsg = {"hello", "hi", "good", "nice"};
    private HandlerThread mHandlerThread;
    private Handler mExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("Socket_Service_Thread");
        mHandlerThread.start();
        mExecutor = new Handler(mHandlerThread.getLooper());
        mExecutor.post(new TCPServer());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isServiceDestroy.set(true);
        mExecutor.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
        super.onDestroy();
    }

    private class TCPServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                //监听本地 8688 端口
                serverSocket = new ServerSocket(23456);
            } catch (IOException e) {
                Log.e(Utils.TAG, "establish tcp server failed port: 23456");
                e.printStackTrace();
                return;
            }
            while (!isServiceDestroy.get()) {
                try {
                    //accept 方法一直阻塞 知道获取到 socket
                    final Socket client = serverSocket.accept();
                    Log.d(Utils.TAG, "accept");

                    responseClient(client);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        // 从客户端读
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        // 往客户端写
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

        while (!isServiceDestroy.get()) {
            String msgFromClient = in.readLine();
            Log.d(Utils.TAG, "msg from client: " + msgFromClient);
            if (TextUtils.isEmpty(msgFromClient)) {
                break;
            }
            int i = new Random().nextInt(mDefinedMsg.length);
            String msg = mDefinedMsg[i];
            out.println(msg);
            //这个 flush 很关键 ,不然客户端没办法读到消息
            out.flush();
            Log.d(Utils.TAG, "server send: " + msg);
        }
        Log.d(Utils.TAG, "client quit");
        out.close();
        in.close();
        client.close();

    }
}
