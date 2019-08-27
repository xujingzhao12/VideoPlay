package com.example.videoplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class VideoDao {
    private List<VideoInfo> videoInfoList = new ArrayList<>();

    /**
     * 获取全部视频
     *
     * @param context
     * @return
     */
    public List<VideoInfo> getVideoInfoList(Context context) {

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] objs = {
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID
        };
        ContentResolver resolver = context.getContentResolver();
        //指定格式查询
        String selection = MediaStore.Video.Media.MIME_TYPE + "=? or "
                         + MediaStore.Video.Media.MIME_TYPE + "=? or "
                         + MediaStore.Video.Media.MIME_TYPE + "=? or "
                         + MediaStore.Video.Media.MIME_TYPE + "=?";
        Cursor cursor = resolver.query(uri, objs, selection, new String[]{"video/mp4", "video/avi", "video/avi", "video/rmvb"}, MediaStore.Video.Media.DATE_MODIFIED);//指定查找视频规则后最后一个就必须要有参数(该查询就是全参查询)
        if (null != cursor) {
            while (cursor.moveToNext()) {
                VideoInfo videoInfo = new VideoInfo(
                        cursor.getString(cursor.getColumnIndex(objs[5])),
                        cursor.getString(cursor.getColumnIndex(objs[0])),
                        cursor.getString(cursor.getColumnIndex(objs[1])),
                        cursor.getString(cursor.getColumnIndex(objs[2])),
                        cursor.getInt(cursor.getColumnIndex(objs[3])),
                        cursor.getLong(cursor.getColumnIndex(objs[4]))
                );
                videoInfoList.add(videoInfo);
            }
            cursor.close();
        }

        return videoInfoList;
    }

    /**
     * 按视频名称删除
     *
     * @param id
     * @param context
     */
    public void delVideo(String id, Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Video.Media._ID + "=?", new String[]{id});
    }

    /**
     * 重命名
     * @param videoInfo
     * @param context
     */
    public void updName(VideoInfo videoInfo,Context context){
        //重新扫描
        MediaScannerConnection.scanFile(context,
                new String[] { videoInfo.getUri() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    //实现回调方法
                    public void onScanCompleted(String path, Uri uri) {
                        //回调方法中的参数即扫描结果

                    }
                });
       /*
       Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DATA, videoInfo.getUri());//该方式实现不了修改名字
        resolver.update(uri, values, MediaStore.Video.Media._ID + "=?", new String[]{videoInfo.getId()});*/
    }
}
