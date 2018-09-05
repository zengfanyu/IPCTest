package com.example.zengfanyu.aildtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.zengfanyu.aildtest.aidl.AIDLActivity;
import com.example.zengfanyu.aildtest.aidl.AIDLService;
import com.example.zengfanyu.aildtest.binderpool.BinderPoolActivity;
import com.example.zengfanyu.aildtest.file.FileActivity;
import com.example.zengfanyu.aildtest.file.User;
import com.example.zengfanyu.aildtest.messenger.MessengerActivity;
import com.example.zengfanyu.aildtest.socket.SocketActivity;
import com.example.zengfanyu.aildtest.utils.Constants;
import com.example.zengfanyu.aildtest.utils.PermissionUtils;
import com.example.zengfanyu.aildtest.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Button mAIDLActivity, mPersistToFile, mBinderPoolActivity, mMessengerActivity, mSocketActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE};
        PermissionUtils.needsPermissions(this, permissions);


        mAIDLActivity = findViewById(R.id.id_btn_aidl_activity);
        mPersistToFile = findViewById(R.id.id_btn_start_persist_to_file);
        mMessengerActivity = findViewById(R.id.id_btn_start_Messenger_activity);
        mSocketActivity = findViewById(R.id.id_btn_start_Socket_activity);
        mBinderPoolActivity = findViewById(R.id.id_btn_start_binder_pool_activity);

        //通过 AIDL 的方式进行跨进程数据共享
        mAIDLActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AIDLActivity.class);
                startActivity(intent);
            }
        });
        //通过共享本地文件的方式进行跨进程数据共享
        mPersistToFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                persistToFile();
                Intent intent2 = new Intent(MainActivity.this, FileActivity.class);
                startActivity(intent2);
            }
        });
        //通过 Messenger 的方式进行跨进程数据共享
        mMessengerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this, MessengerActivity.class);
                startActivity(intent2);
            }
        });

        mSocketActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketActivity.class);
                startActivity(intent);
            }
        });
        mBinderPoolActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BinderPoolActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    /**
     * 序列化 User 对象到本地
     */
    private void persistToFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.TAG, "persist run");
                User user = new User(0, "ZFY", true);

                File dir = new File(Utils.DIR_PATH);
                if (!dir.exists()) {
                    boolean mkdirs = dir.mkdirs();
                    Log.d(Utils.TAG, "mkdirs: " + mkdirs);
                }
                File cacheFile = new File(dir, Utils.FILE_NAME);
                if (!cacheFile.exists()) {
                    try {
                        boolean newFile = cacheFile.createNewFile();
                        Log.d(Utils.TAG, "newFile: " + newFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ObjectOutputStream oos = null;

                try {
                    oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
                    oos.writeObject(user);
                    Log.d(Utils.TAG, "persist user: " + user);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
