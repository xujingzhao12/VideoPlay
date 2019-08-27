package com.example.videoplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<VideoInfo> list = null;
    private Context mContext;
    private VideoInfoAdapter adapter = null;
    private ListView lv;
    private VideoDao videoDao = new VideoDao();
    private Handler handler;
    private ArrayList<String> listPath;
   /* //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }*/


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//设置指定的页面布局

       /* //动态授权
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }*/

        //获取上上文对象,供查询数据使用
        mContext = MainActivity.this;

        //视频列表
        list = videoDao.getVideoInfoList(mContext);
        if (list.isEmpty()) {
            Toast.makeText(mContext, "手机上没有视频文件", Toast.LENGTH_SHORT).show();
        }
        lv = (ListView) findViewById(R.id.lv);
        adapter = new VideoInfoAdapter(list, mContext);
        lv.setAdapter(adapter);
       /* //用来清空手机视频数据
       Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = getContentResolver();
        resolver.delete(uri, null,null);*/
        //System.out.println(list.get(0).toString()+"--------");

        //异步刷新封面
        handler = new Handler() {
            @Override
            //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    //刷新适配器
                    adapter.notifyDataSetChanged();
                }
            }
        };
        //另开一个线程获取视频第一帧
        new Thread(new Runnable() {
            @Override
            public void run() {
                getBitmapList();
            }
        }).start();



        //设置列表背景
        Drawable drawable = getResources().getDrawable(R.drawable.d);
        drawable.setAlpha(100);
        lv.setBackground(drawable);



        listPath = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listPath.add(list.get(i).getUri());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //点击哪个视频播放哪个
                lv.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                VideoInfo videoInfo = list.get(position);
                                Intent intent = new Intent(MainActivity.this, VideoPlay.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("videoInfo", videoInfo);
                                bundle.putStringArrayList("listPath", listPath);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
            }
        }).start();

        //长按菜单
        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    /*contextMenu.add(~)就是Menu的add方法。
                    第一个参数groupId：对菜单项进行分组
                    第二个参数itemId：对应菜单项中的每一项，该参数最关键，用来判断之后点击的是哪个菜单项
                    第三个参数orderId：是控制菜单项的显示顺序的，默认为0，及按照add的顺序显示
                    第四个参数title：就是菜单项上所显示的文字*/
                menu.add(Menu.NONE, 0, 0, "删除");
                menu.add(Menu.NONE, 1, 1, "重命名");
                menu.add(Menu.NONE, 2, 2, "详情");
                //menu.add(Menu.NONE, 2, 2, "编码信息");
                // menu.add(Menu.NONE, 3, 2, "编码格式");
            }
        });

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
    }

    //菜单操作(弹出)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
                /*item.getItemId()是获取菜单项的Id，然后判断点击的是哪个菜单项，去做相应操作
                menuInfo.position()获取的是点击的是listView中的哪个条目的位置*/
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) menuInfo.id;
        final VideoInfo videoInfo = (VideoInfo) adapter.getItem(id);
        switch (item.getItemId()) {
            case 0:
                //删除
                delContextItem(videoInfo);
                return true;
            case 1:
                //重命名
                renameContextItem(videoInfo);
                return true;
            case 2:
                //详情
                try {
                    showContextItem(videoInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
           /* case 3:
                //详情
                try {
                    showCodec(videoInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;*/
        }
        return MainActivity.super.onContextItemSelected(item);
    }

    /*private void showCodec(VideoInfo videoInfo) throws IOException {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(videoInfo.getUri());
        mediaPlayer.prepare();
        int videoHeight = mediaPlayer.getVideoHeight();
        int videoWidth = mediaPlayer.getVideoWidth();//获取视频长和宽
    }*/

    //删除
    public void delContextItem(final VideoInfo videoInfo) {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("删除");
        normalDialog.setMessage("你确定要删除吗?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        list.remove(videoInfo);
                        adapter.notifyDataSetChanged();//首先让界面刷新
                        //再开一个线程进行删除
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                videoDao.delVideo(videoInfo.getId(), mContext);
                            }
                        }).start();
                        Toast.makeText(mContext, "删除", Toast.LENGTH_SHORT).show();
                    }
                });
        normalDialog.setNegativeButton("取消", null);
        // 显示
        normalDialog.show();
    }

    //重命名
    public void renameContextItem(final VideoInfo videoInfo) {
        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.rename, null);
        customizeDialog.setTitle("重命名");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String uri = videoInfo.getUri();//全路径
                        String[] strs = rePath(uri);//文件名,所在目录,格式
                        int i = list.indexOf(videoInfo);
                        VideoInfo newVideoInfo = list.get(i);
                        // 获取EditView中的输入内容
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);
                        String rename = edit_text.getText().toString();
                        if (!("".equals(edit_text)) || !strs[0].equals(rename)) {//新名字不为空或者新名字与旧名字不相同才会修改
                            File oldName = new File(uri);
                            String newUri = strs[1] + rename + "." + strs[2];//新的全路径
                            File newName = new File(newUri);
                            oldName.renameTo(newName);//真正的重命名操作
                            newVideoInfo.setUri(newUri);
                            adapter.notifyDataSetChanged();
                            videoDao.updName(newVideoInfo, mContext);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    videoDao.delVideo(videoInfo.getId(), mContext);//删除原来的文件
                                }
                            }).start();
                            Toast.makeText(mContext, "重命名成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        customizeDialog.show();
        customizeDialog.setNegativeButton("取消", null);
    }

    //详情
    public void showContextItem(VideoInfo videoInfo) throws IOException {
        String uri = "路径:" + videoInfo.getUri();
        String[] names = uri.split("/");
        String[] split = names[names.length - 1].split("\\.");
        String split0 = "名字:" + split[0];
        String split1 = "格式:" + split[1];
        String artist = "作者:" + videoInfo.getArtist();
        int time = (int) (videoInfo.getDuration()) / 1000;
        int m = time / 60;      //分钟
        int s = time - m * 60;    //秒
        int h = time / 3600;      //小时
        String timeStr = "";
        if (h <= 0) {
            if (m > 9) {
                if (s > 9) {
                    timeStr += m + ":" + s;
                } else {
                    timeStr += m + ":0" + s;
                }
            } else {
                if (s > 9) {
                    timeStr += "0" + m + ":" + s;
                } else {
                    timeStr += "0" + m + ":0" + s;
                }
            }
        } else {
            timeStr += h + ":" + m + ":" + s;
        }
        String duration = "时长:" + timeStr;
        long videoSize = videoInfo.getSize() / 1024;
        String size = "大小:" + videoSize + "kB";
        String _id = "标识:" + videoInfo.getId();
        //编码信息
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(videoInfo.getUri());
        mediaPlayer.prepare();
        int videoHeight = mediaPlayer.getVideoHeight();
        int videoWidth = mediaPlayer.getVideoWidth();//获取视频长和宽
        String resolution="分辨率:"+videoWidth+"x"+videoHeight;//分辨率
        String codec="视频码率:"+videoSize*8/time+"kB/s";//码率

        AlertDialog.Builder listDia = new AlertDialog.Builder(MainActivity.this);
        listDia.setTitle("详情");
        listDia.setItems(new String[]{split0, artist, duration, size, split1, _id, uri,codec,resolution}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDia.show();
    }

    //创建菜单(导航栏菜单)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    //菜单触发事件(导航栏)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //扫描
        if (id == R.id.menu_scan) {
            refresh();
        } else if (id == R.id.menu_count) {//统计信息
            menuCount();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean refresh() {
        list.clear();//删除原来的数据
        list = videoDao.getVideoInfoList(mContext);//重新获取数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                getBitmapList();
            }
        }).start();
        adapter.notifyDataSetChanged();//刷新适配器
        Toast.makeText(mContext, "视频列表已经更新", Toast.LENGTH_SHORT).show();
        return true;
    }

    //导航菜单统计按钮
    public boolean menuCount() {
        int size = list.size();//视频个数
        long timeCount = 0;
        long fileSize = 0;
        for (int i = 0; i < list.size(); i++) {
            timeCount += list.get(i).getDuration();
            fileSize += list.get(i).getSize();
        }
        float GSzie = (float) fileSize / 1024 / 1024 / 1024;
        BigDecimal bigDecimal1 = new BigDecimal(GSzie);
        BigDecimal setScaleSize = bigDecimal1.setScale(3, BigDecimal.ROUND_HALF_UP);
        String str = "总共有" + size + "个";
        String strSize = "视频总共是" + setScaleSize + "G大小";
        long time = timeCount / 1000;//总秒数
        float HTime = (float) time / 60 / 60;
        BigDecimal bigDecimal2 = new BigDecimal(HTime);
        BigDecimal setScaleTime = bigDecimal2.setScale(4, BigDecimal.ROUND_HALF_UP);
        String strTime = "视频总时长" + setScaleTime + "小时";
        AlertDialog.Builder listDia = new AlertDialog.Builder(MainActivity.this);
        listDia.setTitle("视频总信息");
        listDia.setItems(new String[]{str, strSize, strTime}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDia.show();
        return true;
    }


    //获取每个视频的第一帧作为封面(是一个耗时操作)
    public void getBitmapList() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        for (int i = 0; i < list.size(); i++) {
            VideoInfo videoInfo = list.get(i);
            String path = videoInfo.getUri();
            metadataRetriever.setDataSource(path);
            Bitmap frameAtTime = metadataRetriever.getFrameAtTime();
            videoInfo.setBitmap(frameAtTime);
            //获取了某个视频的第一帧就给个消息适配器进行更新
            Message msg = new Message();
            msg.what = 0x123;
            handler.sendMessage(msg);
        }
    }

    //解决不能序列化的方案:将该对象转化为字节数组
    public static byte[] getBytes(Bitmap bitmap) {
        //实例化字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);//压缩位图
        return baos.toByteArray();//创建分配字节数组
    }

    public static Bitmap getBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);//从字节数组解码位图
    }

    //获取文件名和除名字外的路径
    public String[] rePath(String oldPathName) {
        String[] names = new String[3];
        String[] split = oldPathName.split("/");
        String s = split[split.length - 1];//原来文件名称及后缀
        String[] ss = s.split("\\.");
        String splitUri = "";
        for (int i = 0; i < split.length - 1; i++) {
            if (0 == i) {
                splitUri += split[i];
            } else {
                splitUri += "/" + split[i];
            }
        }
        splitUri += "/";
        names[2] = ss[1];//文件的格式
        names[1] = splitUri;//文件所在的目录
        names[0] = ss[0];//原来文件的名字
        return names;
    }

    //根据文件URI获取文件mime类型
    public String getMimeType() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        System.out.println(rootPath + "+++++++++++++++++++");
        File rootFile = new File("/storage/emulated/0/qqmusic/mv/杨千嬅 - 再见二丁目 (2010世界巡回演唱会)_v0019n7kosk_3_0 [mqms].mp4");
        String mimeType = "";
        try {
            mimeType = rootFile.toURI().toURL().openConnection().getContentType();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return mimeType;
    }
}
