package com.zhangli.myapplication.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangli.myapplication.R;
import com.zhangli.myapplication.musicUtils.Music;
import com.zhangli.myapplication.service.MusicService;
import com.zhangli.myapplication.utils.BaseTools;

import java.io.File;
import java.util.ArrayList;


public class TwoMusicListActivity extends Activity implements OnItemClickListener, View.OnClickListener {
    private ListView mListView;
    private Handler mHandler = new Handler();
    private MusicListAdapter mAdapter;
    private ArrayList<Music> mMediaLists = new ArrayList<>();
    private Button mLocalScanBtn;
    private Activity activity;
    private ViewHolder holder;
    private int mPlayingPosition;
    private MyBroadcastToast myBroadToast;
    private boolean b = true;

    private TextView nameTxt, artistTxt;
    private ImageView musicImg, palyImg, nextImg, prceImg;
    private Bitmap bitmap;
    private Music music;
    private Intent intentService;
    private RelativeLayout music_list_view;
    private MusicListAdapter adapter;

    //-------------notification------------
    public NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private boolean isPlay = false;

    /**
     * TAG
     */
    private final static String TAG = "CustomActivity";
    /**
     * NotificationCompat 构造器
     */
    NotificationCompat.Builder mBuilder;
    /**
     * 通知栏按钮广播
     */
    public ButtonBroadcastReceiver bReceiver;
    /**
     * 通知栏按钮点击事件对应的ACTION
     */
    public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";

    //-------------------notification----------------------

    //设置一个postion，点击歌曲就得到它
    private int myPostion = 0;

    public MusicService.MyMusicService myGetTimeClass;

    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myGetTimeClass = (MusicService.MyMusicService) service;
            music = mMediaLists.get(mPlayingPosition);
            allPalyMusic();
            mySetImg();


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
        setContentView(R.layout.activity_music_list_layout);
        activity = this;

        mListView = (ListView) findViewById(R.id.music_list_view);
        mListView.setOnItemClickListener(this);
        mLocalScanBtn = (Button) findViewById(R.id.start_saomiao_btn);
        musicImg = (ImageView) findViewById(R.id.music_below_img);
        musicImg.setOnClickListener(this);
        palyImg = (ImageView) findViewById(R.id.paly_below_img);
        palyImg.setOnClickListener(this);
        nextImg = (ImageView) findViewById(R.id.next_below_img);
        nextImg.setOnClickListener(this);
        prceImg = (ImageView) findViewById(R.id.prce_below_img);
        prceImg.setOnClickListener(this);
        nameTxt = (TextView) findViewById(R.id.name_below_txt);
        artistTxt = (TextView) findViewById(R.id.artist_below_txt);

        initButtonReceiver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //listlayout
        music_list_view = (RelativeLayout) findViewById(R.id.relayout_id);

        mAdapter = new MusicListAdapter(this);
        mListView.setAdapter(mAdapter);

        //设置按钮背景透明度
        mLocalScanBtn.getBackground().setAlpha(120);
        music_list_view.getBackground().setAlpha(120);

        mLocalScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SaomiaoActivity.class);
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            scanSDCard();
                            asyncQueryMedia();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        //toast的广播
        myBroadToast = new MyBroadcastToast();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("Toast");
        registerReceiver(myBroadToast, intentFilter2);

        intentService = new Intent(TwoMusicListActivity.this, MusicService.class);
    }

    //绑定服务
    public void bindService() {
        //开启服务
        startService(intentService);
        bindService(intentService, mServiceConnection, BIND_AUTO_CREATE);
        Log.e("tag", "ListActivity》》》》》开启和绑定服务");
    }


    private class MyBroadcastToast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("tag", "收到toast广播");
            Toast.makeText(getApplication(), "扫描到" + mMediaLists.size() + "首歌", Toast.LENGTH_SHORT).show();

            if (mMediaLists.size() > 0) {
                nameTxt.setText(mMediaLists.get(0).getTitle());
                Log.e("tag", "listActivity<<<<<<>>>>musicTitle:" + mMediaLists.get(0).getTitle());
                artistTxt.setText(mMediaLists.get(0).getArtist());

                Log.e("tag", "ListActivity>>>>>mMediaLists:" + mMediaLists);
                if (mMediaLists != null) {
                    intentService.putParcelableArrayListExtra("MusicList", mMediaLists);
                }

                //开启服务
                bindService();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mScanSDCardReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        b = false;
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
        if(myBroadToast!=null){
            myBroadToast=null;
        }
        if(bReceiver!=null){
            bReceiver=null;
        }
        mNotificationManager.cancelAll();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addDataScheme("file");
        registerReceiver(mScanSDCardReceiver, filter);
    }

    public void asyncQueryMedia() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaLists.clear();
                queryMusic(Environment.getExternalStorageDirectory() + File.separator);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setListData(mMediaLists);
                    }
                });
            }
        }).start();
    }

    private void scanSDCard() {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }

    private BroadcastReceiver mScanSDCardReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                asyncQueryMedia();
            }
        }
    };

    public void allPalyMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (b) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //设置歌曲名字和作者
                            nameTxt.setText(myGetTimeClass.getMusicNmae());
                            Log.e("tag", "线程中的》》》》musicname:" + myGetTimeClass.getMusicNmae());
                            artistTxt.setText(myGetTimeClass.getMusicAuther());
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

    /**
     * 点击控制栏的按钮做出相应操作
     */
    @Override
    public void onClick(View v) {
        if (mServiceConnection != null && mMediaLists.size() > 0) {
            Log.e("tag", "点击控制栏：：mServiceConnection:" + mServiceConnection);
            Log.e("tag", "点击控制栏：：mMediaLists:" + mMediaLists);
            if (myGetTimeClass.serviceMediaPlayer()) {
                switch (v.getId()) {
                    //图片
                    case R.id.music_below_img:
                        Intent intent = new Intent(this, MusicActivity.class);
                        intent.putParcelableArrayListExtra("MUSIC_LIST", mMediaLists);
                        intent.putExtra("CURRENT_POSTION", myPostion);
                        startActivity(intent);
                        break;
                    //播放
                    case R.id.paly_below_img:
                        //zhengzai播放
                        if (myGetTimeClass.palyMusic()) {
                            palyImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_play));
                        } else {
                            //暂停
                            palyImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
                        }
                        break;
                    //上一首
                    case R.id.prce_below_img:
                        myGetTimeClass.paceMusic();
                        //重置专辑图片
                        myPostion++;
                        mySetImg();
                        palyImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
                        break;
                    //下一首
                    case R.id.next_below_img:
                        myGetTimeClass.nextMusic();
                        //重置专辑图片
                        mySetImg();
                        myPostion--;
                        palyImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
                        break;
                }
            }
        } else {
            Toast.makeText(getApplication(), "请点击扫描歌曲", Toast.LENGTH_SHORT).show();
        }
    }

    private void mySetImg() {
        String muMusicImg = forMusicList(myGetTimeClass.getMusicNmae());
        bitmap = BitmapFactory.decodeFile(muMusicImg);
        if (bitmap != null) {
            Log.e("tag", "bitmap:>>>>>!=null");
            musicImg.setImageBitmap(bitmap);
        } else {
            Log.e("tag", "bitmap:>>>>>==null");
            musicImg.setImageDrawable(getResources().getDrawable(R.drawable.music_img));
        }
    }

    /**
     * 遍历Musiclist，根据歌曲名得到歌曲图片
     */
    public String forMusicList(String musicName) {
        for (int i = 0; i < mMediaLists.size(); i++) {
            if (music.getTitle().equals(musicName)) {
                return music.getImage();
            }
        }
        return null;
    }

    /**
     * 获取目录下的歌曲
     *
     * @param dirName
     */
    public void queryMusic(String dirName) {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DATA + " like ?",
                new String[]{dirName + "%"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor == null) return;

        // id title singer data time image
        Music music;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // 如果不是音乐
            String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != null && isMusic.equals("")) continue;

            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

            if (isRepeat(title, artist)) continue;

            music = new Music();
            music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            music.setTitle(title);
            music.setArtist(artist);
            music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));

            mMediaLists.add(music);
        }
        cursor.close();
    }

    /**
     * 根据音乐名称和艺术家来判断是否重复包含了
     *
     * @param title
     * @param artist
     * @return
     */
    private boolean isRepeat(String title, String artist) {
        for (Music music : mMediaLists) {
            if (title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据歌曲id获取图片
     *
     * @param albumId
     * @return
     */
    private String getAlbumImage(int albumId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    Uri.parse("content://media/external/audio/albums/"
                            + albumId), new String[]{"album_art"}, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); ) {
                result = cursor.getString(0);
                break;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return null == result ? null : result;
    }

    public class MusicListAdapter extends BaseAdapter {
        private ArrayList<Music> list = new ArrayList<>();
        private Context context;


        public void setPlayingPosition(int position) {
            mPlayingPosition = position;
            notifyDataSetChanged();
        }

        public MusicListAdapter(Context context) {
            this.context = context;
        }

        public void setListData(ArrayList<Music> list) {
            this.list = list;
            notifyDataSetChanged();
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

            if (convertView == null) {
                convertView = View.inflate(context, R.layout.music_list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
                holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
                holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
                holder.mark = convertView.findViewById(R.id.music_list_selected);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mPlayingPosition == position)
                holder.mark.setVisibility(View.VISIBLE);
            else
                holder.mark.setVisibility(View.INVISIBLE);

            Music music = (Music) getItem(position);

            Bitmap icon = BitmapFactory.decodeFile(music.getImage());
            holder.icon.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(), R.drawable.music_img) : icon);
            holder.title.setText(music.getTitle());
            holder.artist.setText(music.getArtist());

            return convertView;
        }
    }

    class ViewHolder {
        ImageView icon;
        TextView title;
        TextView artist;
        View mark;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter = (MusicListAdapter) parent.getAdapter();
        adapter.setPlayingPosition(position);

        myPostion = position;

        //给下面的控制栏设置
        mySetImg();
        nameTxt.setText(mMediaLists.get(position).getTitle());
        artistTxt.setText(mMediaLists.get(position).getArtist());

        //notifi
        //开启notifi
        showButtonNotify();
        mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_pause);
        Log.e("tag", "点击歌曲 ：：：：：notifi>>>>>>>设置为正在播放");

        //传数据给MusicActivity
        Intent intent = new Intent(this, MusicActivity.class);
        intent.putParcelableArrayListExtra("MUSIC_LIST", mMediaLists);
        Log.e("tag", "ListMusicActivity,mMediaLists:" + mMediaLists);
        intent.putExtra("CURRENT_POSTION", position);
        Log.e("tag111", "开始跳转》》》》》》》》position" + position + "《《《" + id);
        startActivity(intent);
        Log.e("tag", "开始跳转》》》》》》》》》");


        //将播放图标设为播放状态
        palyImg.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));

        Intent intent4 = new Intent();
        intent4.setAction("service");
        intent4.putExtra("CURRENT_POSTION", position);
        sendBroadcast(intent4);
        Log.e("tag", "ListActivity>>>>>:发送广播（点击歌曲的广播）成功");
    }

    //______________________________________notification_____________________________________

    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 上一首 按钮点击 ID
     */
    public final static int BUTTON_PREV_ID = 1;
    /**
     * 播放/暂停 按钮点击 ID
     */
    public final static int BUTTON_PALY_ID = 2;
    /**
     * 下一首 按钮点击 ID
     */
    public final static int BUTTON_NEXT_ID = 3;
    /**
     * 退出 按钮点击 ID
     */
    public final static int BUTTON_EXIT_ID = 4;

    /**
     * 带按钮的通知栏点击广播接收
     */
    public void initButtonReceiver() {
        bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }

    /**
     * 广播监听按钮点击事件
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (myGetTimeClass.serviceMediaPlayer() && mMediaLists.size() > 0) {
                if (action.equals(ACTION_BUTTON)) {
                    //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                    int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                    switch (buttonId) {
                        case BUTTON_PREV_ID:
                            Log.d(TAG, "上一首");

                            myGetTimeClass.paceMusic();
                            break;
                        case BUTTON_PALY_ID:
                            isPlay = !isPlay;
                            if (myGetTimeClass.palyMusic()) {
                                Log.e("tag", "notifi>>>>>>>设置为暂停");
                                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_play);
                            } else {
                                Log.e("tag", "notifi>>>>>>>设置为播放");
                                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_pause);
                            }
                            showButtonNotify();
                            break;
                        case BUTTON_NEXT_ID:
                            Log.d(TAG, "下一首");

                            myGetTimeClass.nextMusic();
                            break;
                        case BUTTON_EXIT_ID:

                            mNotificationManager.cancelAll();

                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * 带按钮的通知栏
     */
    public void showButtonNotify() {
        mBuilder = new NotificationCompat.Builder(this);
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.app_img_music);
        //API3.0 以上的时候显示按钮，否则消失
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, mMediaLists.get(0).getTitle());
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, mMediaLists.get(0).getArtist());
        //如果版本号低于（3。0），那么不显示按钮
        if (BaseTools.getSystemVersion() <= 9) {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.GONE);
        } else {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);

//            if (isPlay) {
//                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_pause);
//            } else {
//                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_play);
//            }
        }

        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);
		/* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_paly);
		/* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);
        /* 退出 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_EXIT_ID);
        PendingIntent intent_exit = PendingIntent.getBroadcast(this, 4, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_exit, intent_exit);

        mBuilder.setContent(mRemoteViews)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.drawable.app_img_music);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(200, notify);
    }
    //______________________________________notification_____________________________________
}