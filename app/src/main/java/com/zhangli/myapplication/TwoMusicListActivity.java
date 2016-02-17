package com.zhangli.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangli.myapplication.utils.Music;

import java.io.File;
import java.util.ArrayList;


public class TwoMusicListActivity extends Activity implements OnItemClickListener {
    private ListView mListView;
    private Handler mHandler = new Handler();
    private MusicListAdapter mAdapter;
    private ArrayList<Music> mMediaLists = new ArrayList<>();
    private Button mLocalScanBtn;
    private Activity activity;
    private ViewHolder holder;
    private MyBroadcastReceiver myBroadcastReceiver;
    private int mPlayingPosition;
    private MyBroadcastToast myBroadToast;

//    private TextView nameTxt,artistTxt;
//    private ImageView musicImg,palyImg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_layout);
        activity = this;

        mListView = (ListView) findViewById(R.id.music_list_view);
        mListView.setOnItemClickListener(this);
        mLocalScanBtn = (Button) findViewById(R.id.start_saomiao_btn);
//        nameTxt= (TextView) findViewById(R.id.name_below_txt);
//        artistTxt= (TextView) findViewById(R.id.name_below_txt);
//        musicImg= (ImageView) findViewById(R.id.music_below_img);
//        palyImg= (ImageView) findViewById(R.id.paly_below_img);
//        palyImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                palyImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
//            }
//        });
        mAdapter = new MusicListAdapter(this);
        mListView.setAdapter(mAdapter);

        //设置按钮背景透明度
        mLocalScanBtn.getBackground().setAlpha(100);

        mLocalScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanSDCard();
                asyncQueryMedia();
                Intent intent = new Intent(activity, SaomiaoActivity.class);
                startActivity(intent);
            }
        });

        //给广播注册（从MusicActivity得到关闭的广播）
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zhangli.finishActivity");
        registerReceiver(myBroadcastReceiver, intentFilter);

        //toast的广播
        myBroadToast=new MyBroadcastToast();
        IntentFilter intentFilter2=new IntentFilter();
        intentFilter2.addAction("Toast");
        registerReceiver(myBroadToast, intentFilter2);

    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }

    private class MyBroadcastToast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("tag","收到toast广播");
            Toast.makeText(getApplication(), "扫描到" + mMediaLists.size() + "首歌", Toast.LENGTH_SHORT).show();
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
        unregisterReceiver(myBroadcastReceiver);
        myBroadcastReceiver = null;
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
        MusicListAdapter adapter = (MusicListAdapter) parent.getAdapter();
        adapter.setPlayingPosition(position);

        Intent intent = new Intent(this, MusicActivity.class);
        intent.putParcelableArrayListExtra("MUSIC_LIST", mMediaLists);
        Log.e("tag", "ListMusicActivity,mMediaLists:" + mMediaLists);
        intent.putExtra("CURRENT_POSTION", position);
        Log.e("tag111", "开始跳转》》》》》》》》position" + position + "《《《" + id);
        startActivity(intent);
        Log.e("tag", "开始跳转》》》》》》》》》");

        Intent intent2 = new Intent();
        intent2.setAction("notifi");
        sendBroadcast(intent);

//        //下面的小播放器、
//        nameTxt.setText(mMediaLists.get(position).getTitle());
//        artistTxt.setText(mMediaLists.get(position).getArtist());
//        palyImg.setImageDrawable(getResources().getDrawable(R.drawable.stop_music_btn));
    }


    //点击下面的layout条目
    public void layoutClick(View view){
        Intent intent =new Intent(this,MusicActivity.class);
        startActivity(intent);
    }

    //点击播放

    //上一首

    //下一首


}
