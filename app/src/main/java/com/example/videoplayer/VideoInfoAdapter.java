package com.example.videoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.math.BigDecimal;
import java.util.List;

public class VideoInfoAdapter extends BaseAdapter {
    private List<VideoInfo> list;
    private Context mCpntext;

    public VideoInfoAdapter(List<VideoInfo> list, Context mCpntext) {
        this.list = list;
        this.mCpntext = mCpntext;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(mCpntext).inflate(R.layout.video_list, parent, false);
            holder = new ViewHolder();
            holder.img_ico = (ImageView) convertView.findViewById(R.id.vidoe_thumb);
            holder.txt_title = (TextView) convertView.findViewById(R.id.video_title);
            holder.txt_artist = (TextView) convertView.findViewById(R.id.video_artist);
            holder.txt_duration = (TextView) convertView.findViewById(R.id.video_duration);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        VideoInfo videoInfo = list.get(position);
        showListView(videoInfo, holder);
        return convertView;
    }

    //设置信息到各个组件里面
    public void showListView(VideoInfo videoInfo, ViewHolder holder) {
        String path = videoInfo.getUri();
        String[] strs = path.split("/");
        holder.txt_title.setText(strs[strs.length - 1]);
        holder.txt_artist.setText(videoInfo.getArtist());
        if (null != videoInfo.getBitmap()) {
            holder.img_ico.setImageBitmap(videoInfo.getBitmap());
        }
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
        double bsize = videoInfo.getSize() * 1.0;//单位b
        double size = bsize / 1024;//kB
        double mSize = size / 1024;
        //保留两位小数
        BigDecimal bigDecimal = new BigDecimal(mSize);
        BigDecimal setScale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        double gSize = size / 1024 / 1024;//GB
        double kSize = size - mSize*1024;
        //进行if判断后不能显示正确的大小,全部为0
        /*if (mSize>1024){
            holder.txt_duration.setText(timeStr + "  " + gSize + "GB");
        }else {
            if (kSize>1024){*/
                holder.txt_duration.setText(timeStr + "  " + setScale + "MB");
          /*  }else {
                holder.txt_duration.setText(timeStr + "  " + kSize + "KB");
            }
        }*/
    }

    static class ViewHolder {
        ImageView img_ico;
        TextView txt_title;
        TextView txt_artist;
        TextView txt_duration;
    }
}
