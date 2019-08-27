package com.example.videoplayer.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CacheBitmap {
    public static String FILE_PATH = null;//文件目录

    /**
     * 向本地sd卡写网络图片
     * @param fileName
     * @param bitmap
     */
    public static void saveBitmapTolocal(String fileName, Bitmap bitmap) {
        try {
            //创建文件流,指向该路径,文件名叫fileName
            File file = new File(FILE_PATH,fileName);
            //file是具体的图片,需要判断父级目录是否存在,不存在则创建
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                //创建文件目录
                parentFile.mkdirs();
            }
            //将图片保存到本地
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取缓存图片
     * @param fileName
     * @return
     */
    public static Bitmap getBitmapFromLocal(String fileName){
        File file = new File(FILE_PATH, fileName);
        if (file.exists()){
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
