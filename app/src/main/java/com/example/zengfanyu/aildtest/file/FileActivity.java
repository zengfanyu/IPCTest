package com.example.zengfanyu.aildtest.file;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.zengfanyu.aildtest.R;
import com.example.zengfanyu.aildtest.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author: zengfanyu
 * @Data: 2018/9/4
 * @Description: 通过共享文件的方式进行进程间通信
 */
public class FileActivity extends Activity {
    private Button mBtnRecover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        mBtnRecover = findViewById(R.id.id_btn_recover);
        mBtnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverFromFile();
            }
        });
    }

    private void recoverFromFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = null;
                File file = new File(Utils.DIR_PATH, Utils.FILE_NAME);
                if (file.exists()) {
                    ObjectInputStream ois = null;
                    try {
                        ois = new ObjectInputStream(new FileInputStream(file));
                        user = (User) ois.readObject();
                        Log.d(Utils.TAG, "recover user: " + user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (ois != null) {
                            try {
                                ois.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }
}
