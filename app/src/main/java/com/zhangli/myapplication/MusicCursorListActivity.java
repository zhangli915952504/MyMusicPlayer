package com.zhangli.myapplication;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * MediaStore.Audio.Media.EXTERNAL_CONTENT_URI 对应字段
 * 歌曲ID：MediaStore.Audio.Media._ID
 * 歌曲的名称：MediaStore.Audio.Media.TITLE
 * 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
 * 歌曲的歌手名：MediaStore.Audio.Media.ARTIST
 * 歌曲文件的路径：MediaStore.Audio.Media.DATA
 * 歌曲的总播放时长：MediaStore.Audio.Media.DURATION
 * 歌曲文件的大小：MediaStore.Audio.Media.SIZE
 */

public class MusicCursorListActivity extends Activity implements OnItemClickListener{
    private ListView mListView;
    private FileCursorAdapter mAdapter;
    private ArrayList<MusicBean> mMusicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.music_cursor_list_layout);
        mListView = (ListView) findViewById(R.id.music_list_view);

        /**
         * Android自身具有维护媒体库的功能
         * 1.系统创建了一个SQLITE数据库存放所有音乐资源
         * 2.MediaScaner类负责扫描系统文件，添加音乐资源到数据库 。
         * 什么时间执行扫描操作：1.启动手机,2.插入拔出Sdcard时,3.接收到扫描广播时
         */
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.TITLE_KEY);

        mAdapter = new FileCursorAdapter(this, cursor);
        mListView.setAdapter(mAdapter);

    }

    public class FileCursorAdapter extends CursorAdapter {
        private LayoutInflater layoutInflater;

        public FileCursorAdapter(Context context, Cursor c) {
            super(context, c, FLAG_AUTO_REQUERY);
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context arg1, Cursor cursor) {
            TextView articsTxt   = (TextView) view.findViewById(R.id.music_title_txt);
            TextView titleTxt= (TextView) view.findViewById(R.id.music_artics_txt);
            ImageView imageImg= (ImageView) view.findViewById(R.id.image_music);

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            titleTxt.setText(title);
            articsTxt.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            //imageImg.set(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
        }

        @Override
        public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
            return layoutInflater.inflate(R.layout.item_music_cursor_layout,null);
        }

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.e("tag","进去跳转》》》》》》》》");

        Intent intent = new Intent(this, MusicActivity.class);

        intent.putParcelableArrayListExtra("MUSIC_LIST", mMusicList);
        Log.e("tag", "开始跳转》》》》》》》》mMusicList" + mMusicList);
        intent.putExtra("CURRENT_POSTION", position);
        Log.e("tag", "开始跳转》》》》》》》》position" + position);
        startActivity(intent);
        Log.e("tag", "开始跳转》》》》》》》》》");
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
                            + albumId), new String[]{"album_art"}, null,
                    null, null);
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

}
