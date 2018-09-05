// IOnNewBookArrivedListener.aidl
package com.example.zengfanyu.aildtest;
import com.example.zengfanyu.aildtest.Book;

//暴露给客户端的，需要调用的方法
interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
