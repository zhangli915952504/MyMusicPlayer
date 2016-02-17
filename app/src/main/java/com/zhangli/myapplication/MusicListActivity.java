package com.zhangli.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MusicListActivity extends Activity implements OnItemClickListener {
    private ListView mListView;
    private Handler handler = new Handler();
    private ArrayList<MusicBean> mMusicList = new ArrayList<>();
    private FileAdapter mAdapter;
    private MyBroadcastReceiver myBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_layout);

        mListView = (ListView) findViewById(R.id.music_list_view);
        mListView.setOnItemClickListener(this);

        mAdapter = new FileAdapter(this);
        mListView.setAdapter(mAdapter);
        Log.e("tag", "开启Activity））））））））））））））");

        //给广播注册（从ListActivity得到关闭的广播）
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zhangli.finishActivity");
        registerReceiver(myBroadcastReceiver, intentFilter);


        new Thread(new Runnable() {
            @Override
            public void run() {
                //storage/emulated/0
                //Environment.getExternalStorageDirectory()
                File file=new File(Environment.getExternalStorageDirectory()+"/kgmusic/download");
                scanFileList(file);
                /** ListView刷新必须在UI线程中 通过Handler消息机制发送刷新代码到UI主线程执行 */
                Log.e("tag", "11111111111file:"+file);
                handler.post(new Runnable() {
                    @Override
                    public void run(){
                        Log.e("tag","222222222222222");
                        mAdapter.setListData(mMusicList);
                        Log.e("tag","mMusicList:"+mMusicList.size());
                    }
                });
            }
        }).start();
    }
    /**
     * 扫描Sdcard（外部存储）下所有文件
     */
    public void scanFileList(File parentFile) {
        File[] listFile = parentFile.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                File file = listFile[i];
                if (file.isDirectory()) {
                    scanFileList(file);
                } else {
                    if (file.getName().endsWith(".mp3")) {
                        MusicBean musicBean = new MusicBean();
                        String fileName = file.getName();
                        musicBean.setMusicName(fileName.substring(0, fileName.length() - ".mp3".length()));
                        musicBean.setMusicPath(file.getAbsolutePath());
                        mMusicList.add(musicBean);
                    }
                }
            }
        }
    }

    public class FileAdapter extends BaseAdapter {
        private ArrayList<MusicBean> list = new ArrayList<>();
        private LayoutInflater layoutInflater;

        public FileAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        public void setListData(ArrayList<MusicBean> list) {
            this.list = list;
            Log.e("tag","setListData >>>>"+list.size());
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
                convertView = layoutInflater.inflate(R.layout.item_music_list_layout, null);
            }
            MusicBean file = (MusicBean) getItem(position);
            TextView nameTxt = (TextView) convertView;
            nameTxt.setText(file.getMusicName());
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(this, MusicActivity.class);
        intent.putParcelableArrayListExtra("MUSIC_LIST", mMusicList);
        intent.putExtra("CURRENT_POSTION", position);
        Log.e("tag111", "开始跳转》》》》》》》》position" + position + "《《《" + id);
        startActivity(intent);
        Log.e("tag", "开始跳转》》》》》》》》》");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
        myBroadcastReceiver=null;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }
}