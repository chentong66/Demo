package com.example.demo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Player {
    private Thread mAudioThread;
    private int mMinBufferSize;
    private byte[] mData;
    private int mSampleRate;
    private int mChannel;
    private int mFormat;
    private static final int MODE = AudioTrack.MODE_STREAM;
    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public Player(Context ct,int resid) throws IOException{
        this(ct,resid,SAMPLE_RATE,CHANNEL,FORMAT);
    }
    public Player(Context ct, int resid, int sample_rate, int channel, int format) throws IOException {
        AssetFileDescriptor afd = ct.getResources().openRawResourceFd(resid);
        mData = new byte[(int) afd.getLength()];
        InputStream istream = new FileInputStream(afd.getFileDescriptor());
        if (mData.length != istream.read(mData)) {
            throw new IOException();
        }
        istream.close();
        afd.close();
        mMinBufferSize = AudioTrack.getMinBufferSize(sample_rate, channel, format);
        mSampleRate = sample_rate;
        mChannel = channel;
        mFormat = format;
    }
    private class AudioThread implements Runnable {
        @Override
        public void run() {
            AudioTrack audioTrack = new AudioTrack(STREAM_TYPE, mSampleRate, mChannel, mFormat, mMinBufferSize, MODE);
            while(false == Thread.interrupted() && audioTrack.getState() ==  AudioTrack.STATE_INITIALIZED){
                audioTrack.play();
                audioTrack.write(mData,0,mData.length);
            }
            if (audioTrack.getState() ==  AudioTrack.STATE_INITIALIZED) {//初始化成功
                audioTrack.stop();//停止播放
            }
            audioTrack.release();
        }
    }
    public boolean start() {
        //before start a new thread,first stop the currently running thread (if have)
        stop();
        mAudioThread = new Thread(new AudioThread());
        mAudioThread.start();
        return true;
    }

    public void stop() {
        if (mAudioThread != null) {
            if (mAudioThread.getState() != Thread.State.TERMINATED) {
                try {
                    stop_thread(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mAudioThread = null;
        }
    }

    private void stop_thread(int mills) throws InterruptedException {
        if (mAudioThread != null) {
            mAudioThread.interrupt();
            mAudioThread.join(mills);
        }
    }
}