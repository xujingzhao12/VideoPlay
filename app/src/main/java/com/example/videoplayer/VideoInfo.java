package com.example.videoplayer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class VideoInfo implements Serializable {
    private String title;//名称
    private String artist;//作者
    private String uri;//文件绝对路径
    private int duration;//时长
    private long size;//大小
    private String id;//唯一标识
    private transient Bitmap bitmap;//存储视频第一帧,此成员不能被序列化,需要忽略序列化


    //解决不能序列化的方案:将该对象转化为字节数组
    public static byte[] getBytes(Bitmap bitmap){
        //实例化字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);//压缩位图
        return baos.toByteArray();//创建分配字节数组
    }

    public static Bitmap getBitmap(byte[] data){
        return BitmapFactory.decodeByteArray(data, 0, data.length);//从字节数组解码位图
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public VideoInfo() {
    }

    public VideoInfo(String title, String artist, String uri, int duration, long size) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.duration = duration;
        this.size = size;
    }

    public VideoInfo(String id, String title, String artist, String uri, int duration, long size) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.duration = duration;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", uri='" + uri + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
