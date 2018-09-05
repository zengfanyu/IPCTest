// IBookManager.aidl
package com.example.zengfanyu.aildtest;

import com.example.zengfanyu.aildtest.Book;
import com.example.zengfanyu.aildtest.IOnNewBookArrivedListener;

//这个接口中暴露需要给客户端调用的方法
interface IBookManager {
    void addBook(in Book book);
    List<Book> getBookList();
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
