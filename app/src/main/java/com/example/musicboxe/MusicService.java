package com.example.musicboxe;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

import java.io.IOException;

/**
 * Created by yx on 2017/3/24.
 */
public class MusicService extends Service {
    //创建BroadcastReceiver的子类
    MyReceiver serviceReceiver;
    //创建AssetManager，管理音乐资源
    AssetManager am;
    //创建MediaPlayer
    MediaPlayer mPlayer;
    //创建数组，存储音乐
    String[] musics=new String[]{
            "wish.mp3",
            "promise.mp3",
            "beautiful.mp3"
    };

    //status代表音乐播放状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停播放
    int status=0x11;
    //记录当前正在播放的音乐
    int current=0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //实例化AssetManager
        am=getAssets();
        //创建BroadcastReceiver
        serviceReceiver=new MyReceiver();
        //创建IntentFilter
        IntentFilter filter=new IntentFilter();
        //指定BroadcastReceiver监听的Action
        filter.addAction(MusicBox.CTL_ACTION);
        //注册BroadcastReceiver
        registerReceiver(serviceReceiver,filter);

        //创建MediaPlayer
        mPlayer=new MediaPlayer();
        //为MediaPlayer播放完成事件绑定监听器，播放完一首歌后的行为
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                current++;
                if(current >= 3){
                    current = 0;
                }
                //发送广播通知Activity更改图标和文本框
                Intent sendIntent=new Intent(MusicBox.UPDATE_ACTION);
                sendIntent.putExtra("current",current);
                //发送广播，将被Activity组件的BroadcastReceiver接收到
                sendBroadcast(sendIntent);
                //准备并播放音乐
                prepareAndPlay(musics[current]);
            }
        });
        super.onCreate();
    }

    /**
     * BroadcastReceiver的子类，监听Activity传过来的广播，并将状态发送回Activity
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            //获取Activity传过来的数据，默认为-1
            int control=intent.getIntExtra("control",-1);
            switch (control){
                //播放或暂停状态
                case 1:
                    //原来为没有播放状态
                    if(status==0x11){
                        //准备开始播放音乐
                        prepareAndPlay(musics[current]);
                        //状态变为播放
                        status = 0x12;
                    }
                    //原来处于播放状态
                    else if(status==0x12){
                        //暂停播放
                        mPlayer.pause();
                        //状态变为暂停
                        status=0x13;
                    }
                    //原来处于暂停播放状态
                    else if(status==0x13){
                        //开始播放
                        mPlayer.start();
                        //状态变为播放
                        status=0x12;
                    }
                    break;
                //音乐停止
                case 2:
                    //如果原来正在播放或者暂停播放
                    if(status==0x12||status==0x13){
                        //停止播放
                        mPlayer.stop();
                        //状态变为没有播放
                        status = 0x11;
                    }
            }
            //发送广播通知Activity更改状态和文本框
            Intent sendIntent=new Intent(MusicBox.UPDATE_ACTION);
            sendIntent.putExtra("update",status);
            sendIntent.putExtra("current",current);
            //发送广播，将被Activity组件的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    /**
     * 准备MediaPlayer播放指定音乐
     * @param music
     */
    private void prepareAndPlay(String music) {
        try{
            //打开指定音乐文件
            AssetFileDescriptor afd=am.openFd(music);
            mPlayer.reset();
            //使用MediaPlayer加载指定的声音文件
            mPlayer.setDataSource(afd.getFileDescriptor()
                    ,afd.getStartOffset()
                    ,afd.getLength());
            //准备声音
            mPlayer.prepare();
            //播放
            mPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
