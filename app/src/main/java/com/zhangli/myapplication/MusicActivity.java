package com.zhangli.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangli.myapplication.custom.VisualizerView;
import com.zhangli.myapplication.utils.Music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
    private TextView startMusicTimeTex, stopMusicTimeTex, mMusicNameTxt, musicAutherTex,musicNameNotifi;
    private ImageView palyMusicImg, prceMusicImg, nextMusicImg, destroyMusicImg,notifiPaly;
    private SeekBar seekBar;
    private MusicService.MyGetTimeClass myGetTimeClass;
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private Handler mHandler = new Handler();
    private List<Music> musicList = new ArrayList<>();
    private int mCurrentPostion = 0;
    private Music music;
    private int mProgress;
    private boolean b = true;
    private Intent intent;
    private CircleImageView myMusicImg;
    private MyBroadcastReceiver myBroadcastReceiver;
    private VisualizerView visualizerView;
    private Bitmap bitmap;
//    private ServiceReceiver service;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myGetTimeClass = (MusicService.MyGetTimeClass) service;
            if (myGetTimeClass.isPlaying()) {
                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
            }
            allPalyMusic();
            mySetImg();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "服务出错", Toast.LENGTH_SHORT).show();
            myGetTimeClass = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_layout);

        inintView();

        musicList = getIntent().getParcelableArrayListExtra("MUSIC_LIST");
        Log.e("tag", "MusicActivity,musicList:" + musicList);
        mCurrentPostion = getIntent().getIntExtra("CURRENT_POSTION", 0);

        if (musicList != null) {
            music = musicList.get(mCurrentPostion);
            Log.e("tag1111", "musicBean:" + music);

            intent = new Intent(this, MusicService.class);
            intent.putParcelableArrayListExtra("MusicList", (ArrayList<? extends Parcelable>) musicList);
            intent.putExtra("MusicPostion", mCurrentPostion);
        }

        //开启服务 绑定服务
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);


        //注册旋转的广播
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zhangli.music");
        registerReceiver(myBroadcastReceiver, intentFilter);

        //notifi
//        service=new ServiceReceiver();
//        IntentFilter intentFilter1=new IntentFilter();
//        intentFilter1.addAction("next");
//        intentFilter1.addAction("last");
//        intentFilter1.addAction("play");
//        intentFilter1.addAction("cancel");
//        intentFilter1.addAction("notifi");
//        registerReceiver(service, intentFilter1);

    }

    /**
     * 初始化View
     */
    public void inintView() {
        startMusicTimeTex = (TextView) findViewById(R.id.mp3_current_time_txt);
        stopMusicTimeTex = (TextView) findViewById(R.id.mp3_total_time_txt);
        mMusicNameTxt = (TextView) findViewById(R.id.mp3_musicname_txt);
        musicNameNotifi = (TextView) findViewById(R.id.notification_music_title);

        musicAutherTex = (TextView) findViewById(R.id.music_auther_txt);
        palyMusicImg = (ImageView) findViewById(R.id.mp3_playter_music_img);
        prceMusicImg = (ImageView) findViewById(R.id.mp3_prce_music_img);
        nextMusicImg = (ImageView) findViewById(R.id.mp3_next_music_img);
        destroyMusicImg = (ImageView) findViewById(R.id.mp3_destroy_music_img);
        seekBar = (SeekBar) findViewById(R.id.mp3_player_seekbar);

        //notifi
        notifiPaly= (ImageView) findViewById(R.id.notification_paly_song_button);
//        notofiNext= (ImageButton) findViewById(R.id.notification_next_song_button);
//        notifiPace= (ImageButton) findViewById(R.id.notification_pace_song_button);

        //专辑图片
        myMusicImg = (CircleImageView) findViewById(R.id.music_image_imageview);
        myMusicImg.setBorderColor(R.color.beige);
        myMusicImg.setBorderWidth(8);

        //跳动的频率
        visualizerView = (VisualizerView) findViewById(R.id.activity_player_visualizer);

        palyMusicImg.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        prceMusicImg.setOnClickListener(this);
        nextMusicImg.setOnClickListener(this);
        destroyMusicImg.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //播放/暂停
            case R.id.mp3_playter_music_img:
                //zhengzai播放
                if (myGetTimeClass.palyMusic()) {
                    palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.paly_music_btn));
                    myMusicImg.roatatePause();
                } else {
                    //暂停
                    palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
                    myMusicImg.roatateStart();
                }

                break;
            //上一首
            case R.id.mp3_prce_music_img:
                myGetTimeClass.paceMusic();
                //重置专辑图片
                mySetImg();
                myMusicImg.resetRoatate();
                myMusicImg.roatateStart();

                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
                break;
            //下一首
            case R.id.mp3_next_music_img:
                myGetTimeClass.nextMusic();
                //重置专辑图片
                mySetImg();
                myMusicImg.resetRoatate();
                myMusicImg.roatateStart();

                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
                break;
            //銷毀播放器
            case R.id.mp3_destroy_music_img:
                stopService(intent);
                myMusicImg.resetRoatate();
                finish();
                Intent intent = new Intent();
                intent.setAction("com.zhangli.finishActivity");
                sendBroadcast(intent);
                break;
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //旋转的专辑图片
            myMusicImg.roatateStart();
            //音乐频率
            visualizerView.setupVisualizerFx(myGetTimeClass.getMusicId());
        }
    }

    private void mySetImg() {
        String muMusicImg = forMusicList(myGetTimeClass.getMusicNmae());
        bitmap = BitmapFactory.decodeFile(muMusicImg);
        if (bitmap != null) {
            Log.e("tag", "bitmap:>>>>>!=null");
            myMusicImg.setImageBitmap(bitmap);
        } else {
            Log.e("tag", "bitmap:>>>>>==null");
            myMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.music_img));
        }
    }

    public void allPalyMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (b) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int currentTime = myGetTimeClass.getPalyTime();
                            int allTime = myGetTimeClass.getAllTime();

                            //设置歌曲时间和总时间
                            startMusicTimeTex.setText(format.format(currentTime));
                            stopMusicTimeTex.setText(format.format(allTime));

                            //设置进度条
                            seekBar.setMax(myGetTimeClass.getAllTime());
                            seekBar.setProgress(myGetTimeClass.getPalyTime());

                            //设置歌曲名字和作者
                            mMusicNameTxt.setText(myGetTimeClass.getMusicNmae());
                            String auther = "—" + myGetTimeClass.getMusicAuther() + "—";
                            musicAutherTex.setText(auther);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //--------------------监控进度条的状态---------------------------------------------------
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (myGetTimeClass != null) {
            mProgress = progress;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e("tag", "onStartTrackingTouch >>>>> ");
        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.paly_music_btn));
        myGetTimeClass.myStopMusic();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e("tag", "onStopTrackingTouch >>>>> ");
        if (myGetTimeClass != null) {
            myGetTimeClass.seekto(mProgress);
        }
        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
        myGetTimeClass.palyMusic();
    }
//---------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(b==true) {
            myFinish();
        }
    }

    public  void myFinish(){
        if (connection != null) {
            b = false;
            unbindService(connection);
            Log.e("tag", "connection解绑:+" + connection);

            //销毁广播
            unregisterReceiver(myBroadcastReceiver);
            myBroadcastReceiver = null;
            //重置旋转的专辑图片
            myMusicImg.resetRoatate();
            //销毁音乐频率
            visualizerView.releaseVisualizerFx();
        }
    }

    /**
     * 遍历Musiclist，根据歌曲名得到歌曲图片
     */
    public String forMusicList(String musicName) {
        for (int i = 0; i < musicList.size(); i++) {
            if (music.getTitle().equals(musicName)) {
                return music.getImage();
            }
        }
        return null;
    }
    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (myGetTimeClass != null) {
                Log.e("tag","myGetTimeClass<<<<<<<<<<<<<<<<<<<<"+myGetTimeClass);
                if ("play".equals(action)) {
                    //播放
                    if (myGetTimeClass.palyMusic()) {
                        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
                        notifiPaly.setImageDrawable(getResources().getDrawable(R.drawable.notification_stop_btn));
                        myMusicImg.roatateStart();
                    } else {
                        //暂停
                        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.paly_music_btn));
                        notifiPaly.setImageDrawable(getResources().getDrawable(R.drawable.notification_paly_btn));
                        myMusicImg.roatatePause();
                    }
                    Log.e("tag","play<<<<<<<<<<<<<<<<<<<<");
                } else if ("next".equals(action)) {
                    myGetTimeClass.nextMusic();
                    Log.e("tag", "next<<<<<<<<<<<<<<<<<<<<");
                } else if ("last".equals(action)) {
                    myGetTimeClass.paceMusic();
                    Log.e("tag", "last<<<<<<<<<<<<<<<<<<<<");
                } else if ("notifi".equals(action)) {
                    musicNameNotifi.setText(myGetTimeClass.getMusicNmae());
                    notifiPaly.setImageDrawable(getResources().getDrawable(R.drawable.notification_stop_btn));
                }
            }
            if ("cancel".equals(action)) {
                stopService(intent);
                myMusicImg.resetRoatate();
                finish();
                Intent intent2 = new Intent();
                intent2.setAction("com.zhangli.finishActivity");
                sendBroadcast(intent);
                System.exit(0);
                Log.e("tag", "tuichu<<<<<<<<<<<<<<<<<<<<");
            }
        }
    }
    public void backClick(View view){
        myFinish();
        finish();
    }
}



