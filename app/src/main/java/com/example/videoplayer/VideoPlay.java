package com.example.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import com.example.videoplayer.utils.BaseActivity;
import com.example.videoplayer.utils.MmediaController;
import com.example.videoplayer.utils.VideoView;

import java.io.IOException;
import java.util.ArrayList;

public class VideoPlay extends BaseActivity {
    private VideoView player;

    private View contentView;
    private RelativeLayout playerParent;

    private MmediaController mmediaController;


    @Override
    protected int getRecrouse() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play_page);

        player = findViewById(R.id.paly_video);

        //contentView = findViewById(R.id.contentView);
        //titleView = findViewById(R.id.title_view);

        playerParent = (RelativeLayout) findViewById(R.id.player_parent);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        VideoInfo videoInfo = (VideoInfo)bundle.getSerializable("videoInfo");
        ArrayList<String> listPath = bundle.getStringArrayList("listPath");
        //player.setListPath(listPath);为播放模式准备数据
        player.setVideoPath(videoInfo.getUri());
        MediaPlayer mediaPlayer = new MediaPlayer();
        //判断是否为竖屏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏,设置VideoView组件的大小,使之在竖屏播放下画面不至于被拉扯
            try {
                //因为换屏了,这个activity生命结束.所以重新设定资源
                mediaPlayer.setDataSource(videoInfo.getUri());
                mediaPlayer.prepare();
                int videoHeight = mediaPlayer.getVideoHeight();
                int videoWidth = mediaPlayer.getVideoWidth();//获取视频长和宽
                //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1080, (int)(videoHeight/(videoWidth*1.0/1080)));
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1080, 720);
                player.setLayoutParams(layoutParams);
                //播放组件外的区域设为黑色
                playerParent.setBackgroundColor(Color.BLACK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //转屏前数据不为空就跳转到这个进度
        if (savedInstanceState != null){
            //这个方法要在设置播放路径之后
            player.seekTo(savedInstanceState.getInt("key"));
        }
        //设置循环播放
        if (!player.isLooping()){
            player.setLooping();
        }
        player.start();

        mmediaController = new MmediaController(this)
                //.setTitleBar(titleView)
                .setPlayerParent(playerParent)
                .setPlayer(player)
                .setContentView(contentView)
                .build();

    }
    @Override
    protected void initView() {

    }

    @SuppressLint("NewApi")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean tag = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? false : true;
        mmediaController.switchOrientation(tag);
        fullScreen(!tag ? true : false);
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 保存转屏前的播放进度
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("key", mmediaController.seek);
    }
}
