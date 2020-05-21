package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Recorder {
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private static final String PCM_MIC1_SUFFIX = "recorder_mic1.pcm";
    private static final String PCM_MIC2_SUFFIX = "recorder_mic2.pcm";
    private static final String PCM_MIC = "recorder_mic.pcm";

    private Activity mAcvitity;
    private String mDir;
    private String mPcmMic1;
    private String mPcmMic2;
    private String mPcmMic;
    private Thread mRecordThread;
    private int mAudioSource;
    private int mSampleRate;
    private int mAudioFormat;
    private int mChannelConfig;
    public Recorder(String dir, Activity activity){
        this(dir,activity,DEFAULT_AUDIO_SOURCE,DEFAULT_SAMPLE_RATE,DEFAULT_AUDIO_FORMAT,DEFAULT_CHANNEL_CONFIG);
    }
    public Recorder(String dir,Activity activity,int source,int sampleRate,int audioFormat,int channleConfig){
        mDir = dir;
        mPcmMic1 = mDir + File.separator + PCM_MIC1_SUFFIX;
        mPcmMic2 = mDir + File.separator + PCM_MIC2_SUFFIX;
        mPcmMic = mDir + File.separator + PCM_MIC;
        mAcvitity = activity;
        mAudioSource = source;
        mSampleRate = sampleRate;
        mAudioFormat = audioFormat;
        mChannelConfig = channleConfig;
        //Todo:skip the requestPermissions if permissions are already granted.
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 1);
        //Todo:need to check whether the permissions are granted,ifnot,throws an exception.
    }
    protected  String[] getPcmFiles(){
        return new String[] {mPcmMic1,mPcmMic2,mPcmMic};
    }
    protected  String getPcmDir(){
        return mDir;
    }
    protected int getmAudioSource(){
        return mAudioSource;
    }
    protected int getmSampleRate(){
        return mSampleRate;
    }
    protected int getmAudioFormat(){
        return mAudioFormat;
    }
    protected int getmChannelConfig(){
        return mChannelConfig;
    }
    private class RecordThread implements  Runnable {
        @Override
        public void run(){
            String pcm_mic1 = mPcmMic1;
            String pcm_mic2 = mPcmMic2;
            String pcm_mic = mPcmMic;
            System.out.println("Creating pcm file in directory:"+mDir);
            File file_mic1 = new File(pcm_mic1);
            Log.d("Recorder","save file1");
            File file_mic2 = new File(pcm_mic2);
            File file_mic = new File(pcm_mic);
            try {
                if (file_mic1.exists()) {
                    file_mic1.delete();
                }
                if (file_mic2.exists()) {
                    file_mic2.delete();
                }
                if (file_mic.exists())
                    file_mic.delete();
                file_mic1.createNewFile();
                file_mic2.createNewFile();
                file_mic.createNewFile();
                DataOutputStream out_mic1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_mic1)));
                DataOutputStream out_mic2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_mic2)));
                DataOutputStream out_mic = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_mic)));

                int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);
                AudioRecord audioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelConfig, mAudioFormat, bufferSize);

                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    short[] buffer = new short[(bufferSize)];
                    try {
                        audioRecord.startRecording();
                        while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING && (false == Thread.interrupted())) {
                            int audioResult = audioRecord.read(buffer, 0, bufferSize);
                            if (audioResult != AudioRecord.ERROR_INVALID_OPERATION) {
                                for (int i = 0; i < audioResult; i++) {
                                    if (i % 2 == 0) {
                                        out_mic1.write(buffer[i]);
                                        out_mic1.write(buffer[i] >> 8);
                                    } else {
                                        out_mic2.write(buffer[i]);
                                        out_mic2.write(buffer[i] >> 8);
                                    }
                                    out_mic.write(buffer[i]);
                                    out_mic.write(buffer[i] >> 8);
                                }
                            }
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        audioRecord.stop();
                        audioRecord.release();
                        out_mic1.close();
                        out_mic2.close();
                        out_mic.close();
                    }
                }
                else {
                    throw new Exception("audioRecord.getState() != AudioRecord.STATE_INITIALIZED,Record failed.");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public boolean start(){
        //before start a new thread,first stop the currently running thread (if have)
        stop();
        mRecordThread = new Thread(new RecordThread());
        mRecordThread.start();
        return true;
    }
    public void stop(){
        if (mRecordThread != null){
            if (mRecordThread.getState() != Thread.State.TERMINATED) {
                try {
                    stop_thread(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mRecordThread = null;
        }
        PcmToWavUtil pwu = new PcmToWavUtil(this.getmSampleRate(),this.getmChannelConfig(),this.getmAudioFormat());
        try {
            pwu.pcmTowav(this.getPcmFiles()[0], this.getPcmDir() + File.separator + "pcm_mic1.wav");
            pwu.pcmTowav(this.getPcmFiles()[1], this.getPcmDir() + File.separator + "pcm_mic2.wav");
            pwu.pcmTowav(this.getPcmFiles()[2], this.getPcmDir() + File.separator + "pcm_mic.wav");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        assert(mRecordThread == null);
    }
    private void stop_thread(int mills) throws InterruptedException{
        if (mRecordThread != null){
            mRecordThread.interrupt();
            mRecordThread.join(mills);
        }
    }
}
