package com.example.musicboxe;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MusicBox extends Activity implements OnClickListener {
    //获取歌曲名称、歌手名字
    TextView title,author;
    //播放、暂停按钮
    ImageButton play,stop;
    //创建BroadcastReceiver的子类
    ActivityReceiver activityReceiver;

    public static final String CTL_ACTION="com.example.CTL_ACTION";
    public static final String UPDATE_ACTION="com.example.UPDATE_ACTION";

    //status代表音乐播放状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停播放
    int status=0x11;
    String[] titleStrs=new String[]{
            "心愿",
            "约定",
            "美丽新世界"
    };

    String[] authorStrs=new String[]{
            "未知艺术家",
            "周蕙",
            "伍佰"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_box);
        //获取两个按钮、两个TextView
        play=(ImageButton)this.findViewById(R.id.play);
        stop=(ImageButton)this.findViewById(R.id.stop);
        title=(TextView)findViewById(R.id.title);
        author=(TextView)findViewById(R.id.author);
        //为两个按钮添加监听器
        play.setOnClickListener(this);
        stop.setOnClickListener(this);

        //实例化ActivityReceiver
        activityReceiver=new ActivityReceiver();
        //创建IntentFilter
        IntentFilter filter=new IntentFilter();
        //指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        //注册ActivityReceiver
        registerReceiver(activityReceiver,filter);
        //创建Intent，启动MusicService（后台Service）
        Intent intent=new Intent(MusicBox.this,MusicService.class);
        startService(intent);
    }

    /**
     * 根据用户单击的按钮，发送广播给Service
     * @param view
     */
    @Override
    public void onClick(View view) {
        //创建Intent
        Intent intent=new Intent("com.example.CTL_ACTION");
        switch (view.getId()){
            case R.id.play:
                intent.putExtra("control",1);
                break;
            case R.id.stop:
                intent.putExtra("control",2);
                break;
        }
        //发送广播，将被Service组件的BroadcastReceiver接收到
        sendBroadcast(intent);
    }

    /**
     * BroadcastReceiver的子类，监听Service传回来的广播
     */
    public class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取Intent中的update消息，update代表播放状态
            int update=intent.getIntExtra("update",-1);
            //获取Intent中的current消息，current代表当前正在播放的音乐
            int current=intent.getIntExtra("current",-1);
            //当前有音乐时，显示该音乐的名称和作者
            if(current>=0){
                title.setText(titleStrs[current]);
                author.setText(authorStrs[current]);
            }
            //根据播放状态显示播放、暂停图标
            switch (update){
                //没有播放状态
                case 0x11:
                    play.setImageResource(R.drawable.play);
                    status=0x11;
                    break;
                //播放状态
                case 0x12:
                    play.setImageResource(R.drawable.pause);
                    status=0x12;
                    break;
                //暂停状态
                case 0x13:
                    play.setImageResource(R.drawable.play);
                    status=0x13;
                    break;
            }
        }
    }
}
