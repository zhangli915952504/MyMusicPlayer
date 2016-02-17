package com.zhangli.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;


public class SaomiaoActivity extends Activity {
    private ImageView imageView;
    private AnimationDrawable animationDrawable;
    private Handler mHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saomiao_layout);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.image_values);
        animationDrawable = (AnimationDrawable) imageView.getDrawable();
        //是否循环一次
        animationDrawable.setOneShot(false);
        animationDrawable.start();
        MyThread();
    }

    private void MyThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5500);
                    animationDrawable.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        Intent intent=getIntent();
//                        int listSize=intent.getIntExtra("ListSize",0);
//                        Toast.makeText(getApplication(), "扫描到" + listSize + "首歌", Toast.LENGTH_SHORT).show();

                        Log.e("tag", "发送toast广播");
                        Intent toastIntent = new Intent();
                        toastIntent.setAction("Toast");
                        sendBroadcast(toastIntent);
                        Log.e("tag", "发送toast广播成功");
                        finish();
                    }
                });
            }
        }).start();
    }

}
