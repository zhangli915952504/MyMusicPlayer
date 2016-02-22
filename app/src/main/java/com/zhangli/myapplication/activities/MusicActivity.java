package com.zhangli.myapplication.activities;

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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangli.myapplication.R;
import com.zhangli.myapplication.custom.VisualizerView;
import com.zhangli.myapplication.musicUtils.Music;
import com.zhangli.myapplication.service.MusicService;
import com.zhangli.myapplication.utils.CircleImageView;
import com.zhangli.myapplication.utils.PeriscopeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//import android.support.v7.widget.PopupMenu;

public class MusicActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
    private TextView startMusicTimeTex, stopMusicTimeTex, mMusicNameTxt, musicAutherTex, musicNameNotifi;
    private ImageView palyMusicImg, prceMusicImg, nextMusicImg, destroyMusicImg, notifiPaly;
    private SeekBar seekBar;
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private Handler mHandler = new Handler();
    private List<Music> musicList = new ArrayList<>();
    private int mCurrentPostion;
    private Music music;
    private int mProgress;
    private boolean b = true;
//    private Intent intent;
    private CircleImageView myMusicImg;
    private MyBroadcastReceiver myBroadcastReceiver;
    private VisualizerView visualizerView;
    private Bitmap bitmap;
    //心形气泡
    private PeriscopeLayout periscopeLayout;
    private boolean temp = true;


    public MusicService.MyMusicService myGetTimeClass;

    public ServiceConnection mServerceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myGetTimeClass = (MusicService.MyMusicService) service;
            Log.e("tag", "listActivity::::++mCurrentPostion:" + mCurrentPostion);
            music = musicList.get(mCurrentPostion);
            allPalyMusic();
            mySetImg();
            if (myGetTimeClass.isPlaying()) {
                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
            }

            Intent intent4 = new Intent();
            intent4.setAction("xuanzhuan");
            sendBroadcast(intent4);
            Log.e("tag", "ListActivity>>>>>:发送广播（旋转图片）成功");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myGetTimeClass = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_music_layout);

        inintView();

        musicList = getIntent().getParcelableArrayListExtra("MUSIC_LIST");
        Log.e("tag", "MusicActivity,musicList:" + musicList);
        mCurrentPostion = getIntent().getIntExtra("CURRENT_POSTION", 0);

        Log.e("tag", "ListActivity传过来的postion:::::" + mCurrentPostion);
        //注册旋转的广播
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("xuanzhuan");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    //绑定服务
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mServerceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 初始化View
     */
    public void inintView() {
        startMusicTimeTex = (TextView) findViewById(R.id.mp3_current_time_txt);
        stopMusicTimeTex = (TextView) findViewById(R.id.mp3_total_time_txt);
        mMusicNameTxt = (TextView) findViewById(R.id.mp3_musicname_txt);

        musicAutherTex = (TextView) findViewById(R.id.music_auther_txt);
        palyMusicImg = (ImageView) findViewById(R.id.mp3_playter_music_img);
        prceMusicImg = (ImageView) findViewById(R.id.mp3_prce_music_img);
        nextMusicImg = (ImageView) findViewById(R.id.mp3_next_music_img);
        destroyMusicImg = (ImageView) findViewById(R.id.mp3_destroy_music_img);
        seekBar = (SeekBar) findViewById(R.id.mp3_player_seekbar);
        periscopeLayout= (PeriscopeLayout) findViewById(R.id.periscope);


        //专辑图片
        myMusicImg = (CircleImageView) findViewById(R.id.music_image_imageview);
        myMusicImg.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            //播放/暂停
            case R.id.mp3_playter_music_img:
                //zhengzai播放
                if (myGetTimeClass.palyMusic()) {
                    palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_play));
                    myMusicImg.roatatePause();
                } else {
                    //暂停
                    palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
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

                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
                break;
            //下一首
            case R.id.mp3_next_music_img:
                myGetTimeClass.nextMusic();
                //重置专辑图片
                mySetImg();
                myMusicImg.resetRoatate();
                myMusicImg.roatateStart();

                palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
                break;
            //菜单按钮
            case R.id.mp3_destroy_music_img:

                showPopupMenu(destroyMusicImg);

                break;
            case R.id.music_image_imageview:
                if (temp) {
                    new MyTimer(5000, 200).start();
                    temp = false;
                }
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
        Log.e("tag", "MusicActivity>>>+myGetTimeClass:" + myGetTimeClass);
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

                            mySetImg();
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
        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_play));
        myGetTimeClass.myStopMusic();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e("tag", "onStopTrackingTouch >>>>> ");
        if (myGetTimeClass != null) {
            myGetTimeClass.seekto(mProgress);
        }
        palyMusicImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
        myGetTimeClass.palyMusic();
    }
//---------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (b == true) {
            myFinish();
        }
    }

    public void myFinish() {
        if (mServerceConnection != null) {
            b = false;
            unbindService(mServerceConnection);
            Log.e("tag", "connection解绑:+" + mServerceConnection);

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



    public void backClick(View view) {
        myFinish();
        finish();
    }

    public class MyTimer extends CountDownTimer {

        private static final String TAG = "MyTimer";

        //millisInFuture为你设置的此次倒计时的总时长，比如60秒就设置为60000
        //countDownInterval为你设置的时间间隔，比如一般为1秒,根据需要自定义。
        public MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //每过你规定的时间间隔做的操作
        @Override
        public void onTick(long millisUntilFinished) {
            periscopeLayout.addHeart();
            Log.d(TAG, "111");
        }

        //倒计时结束时做的操作↓↓
        @Override
        public void onFinish() {
            temp = true;
        }

    }

    //设置菜单   扫描二维码
   private void showPopupMenu(View view) {
             // View当前PopupMenu显示的相对View的位置
           PopupMenu popupMenu = new PopupMenu(this, view);

          // menu布局
            popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

                // menu的item点击事件
              popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                      Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();

                      Intent intent =new Intent(MusicActivity.this,ErweimaActivity.class);
                      startActivity(intent);
                      return false;
                  }
              });

              // PopupMenu关闭事件
               popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                   @Override
                   public void onDismiss(PopupMenu menu) {

                   }
               });

              popupMenu.show();
           }

}



