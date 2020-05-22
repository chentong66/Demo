package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
  //  private MediaPlayer mediaPlayer;
    private Recorder mRecorder;
    private Player mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     //  mediaPlayer = MediaPlayer.create(this, R.raw.chirp);
        try {
            mPlayer = new Player(this, R.raw.chirp);
        }
        catch(Exception e){
            System.out.println("Exception");
            assert (false);
        }
        mRecorder = new Recorder(Environment.getExternalStorageDirectory().getAbsolutePath(), this);
        Button btn_play = (Button) findViewById(R.id.play);
        Button btn_pause = (Button) findViewById(R.id.pause);
        Button btn_stop = (Button) findViewById(R.id.stop);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.start(); // 开始播放
                mRecorder.start();
            }
        });

  /*      btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.pause();
            }
        });
   */

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.stop();
                mRecorder.stop();
           //     mediaPlayer.release();
            }
        });
    }
}
