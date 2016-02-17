package com.zhangli.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zhangli.myapplication.utils.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private static List<Music> musicList = new ArrayList<>();
    private static int postion;
    private static MediaPlayer mediaPlayer;


    @Override
    public IBinder onBind(Intent intent) {
        return mgetTimeClass;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("tag", "进入预播放阶段");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        musicList = intent.getParcelableArrayListExtra("MusicList");
        postion = intent.getIntExtra("MusicPostion", 0);
        Log.e("tag", "musicList:" + musicList);
        Log.e("tag", "postion:" + postion);
        inintMusic();
        palyerMusic();

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private interface GetTime {
        //正在播放的音乐时间
        int getPalyTime();

        //总的音乐时间
        int getAllTime();

        //得到歌曲名字
        String getMusicNmae();


        //是否播放
        boolean isPlaying();

        //播放音乐
        boolean palyMusic();

        //关联音乐到滚动条上，即拉动滚动条，音乐播放滚动条当前位置
        //将seekbar当前的位置设置到音乐的时间上
        void seekto(int i);

        //上一首
        void paceMusic();

        //下一首
        void nextMusic();


        void myStopMusic();


        //音乐的id
        int getMusicId();

        //作者
        String getMusicAuther();
    }

    public void inintMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            Log.e("tag", "inintMusic()>>>>>>>>>>:" + mediaPlayer);
        }
        try {
            mediaPlayer.reset();
            Log.e("tag33", "reset>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            mediaPlayer.setDataSource(musicList.get(postion).getUri());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e("tag33", "播放下一首");

                    mgetTimeClass.nextMusic();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class MyGetTimeClass extends Binder implements GetTime {

        @Override
        public int getPalyTime() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        }

        @Override
        public int getAllTime() {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            } else {
                return 0;
            }
        }

        @Override
        public String getMusicNmae() {
            if (mediaPlayer != null) {
                return musicList.get(postion).getTitle();
            } else {
                return "";
            }
        }

        @Override
        public boolean isPlaying() {
            if (mediaPlayer.isPlaying()) {
                //发送一个旋转的广播
                Intent intent = new Intent();
                intent.setAction("com.zhangli.music");
                sendBroadcast(intent);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean palyMusic() {
            return palyerMusic();
        }

        @Override
        public void seekto(int i) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(i);
            }
        }

        @Override
        public void paceMusic() {
            postion--;
            if (postion < 0) {
                postion = 0;
            }
            inintMusic();
            palyMusic();
        }

        @Override
        public void nextMusic() {
            postion++;
            if (postion > (musicList.size() - 1)) {
                postion = musicList.size() - 1;
            }
            inintMusic();
            palyMusic();
        }

        @Override
        public void myStopMusic() {
            onDestroy();
        }

        @Override
        public int getMusicId() {
            return mediaPlayer.getAudioSessionId();
        }


        @Override
        public String getMusicAuther() {
            return musicList.get(postion).getArtist();
        }
    }

    public MyGetTimeClass mgetTimeClass = new MyGetTimeClass();

    public boolean palyerMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            return true;
        } else {
            mediaPlayer.start();
            return false;
        }
    }


}
