package com.example.demo;



import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcmToWavUtil {
    //采样率
    private int mSampleRateInHz;
    //声道数
    private int mChannelConfig;
    //最小缓冲区大小
    private int mBufferSizeInBytes;
    //数据格式
    //这里传入了AudioFormat.ENCODING_PCM_16BIT,[所以下面代码中的每样值位数为16，每样值字节为2后面也会用]
    private int mAudioFormat;

    public PcmToWavUtil(int mSampleRateInHz , int mChannelConfig ,int mAudioFormat){
        this.mSampleRateInHz = mSampleRateInHz;
        this.mChannelConfig = mChannelConfig;
        this.mAudioFormat = mAudioFormat;

        this.mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz,mChannelConfig,mAudioFormat);

    }

    public void pcmTowav(String pcmfilepath , String wavfilepath ) throws IOException {
        FileInputStream pcmIn;
        FileOutputStream wavOut;
        //原始pcm数据大小不含(文件头),添加文件头要用
        long pcmLength;
        //文件总大小(含文件头),添加文件头要用
        long dataLength;
        //通道标识（1(单通道)或2(双通道)，添加文件头要用）
        int channels = (mChannelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2);
        //采样率，添加文件头要用
        int sampleRate = mSampleRateInHz;
        //信息传输速率=((采样率*通道数*每样值位数) / 8),添加文件头要用
        int byteRate = sampleRate*channels*mAudioFormat/8;

        byte[] data = new byte[mBufferSizeInBytes];
        pcmIn = new FileInputStream(pcmfilepath);
        wavOut = new FileOutputStream(wavfilepath);
        pcmLength = pcmIn.getChannel().size();
        //wav文件头44字节
        dataLength = pcmLength+44;
        //先写入wav文件头
        System.err.println("Channels:"+channels+",SampleRate:"+sampleRate);
        writeHeader(wavOut , pcmLength , dataLength , sampleRate , channels , byteRate);
        //再写入数据
        while (pcmIn.read(data)!=-1){
            wavOut.write(data);
        }
        Log.i("TAG","wav文件写入完成");
        pcmIn.close();
        wavOut.close();
    }

    private void writeHeader(FileOutputStream wavOut, long pcmLength, long dataLength, int sampleRate, int channels, int byteRate) throws IOException {
        //wave文件头44个字节
        byte[] header = new byte[44];
        /*0-11字节(RIFF chunk ：riff文件描述块)*/
        header[0]='R';
        header[1]='I';
        header[2]='F';
        header[3]='F';
        header[4]= (byte) (dataLength * 0xff); //取一个字节（低8位）
        header[5]= (byte) ((dataLength >> 8) * 0xff); //取一个字节 （中8位）
        header[6]= (byte) ((dataLength >> 16) * 0xff); //取一个字节 (次8位)
        header[7]= (byte) ((dataLength >> 24) * 0xff); //取一个字节 （高8位）
        header[8]='W';
        header[9]='A';
        header[10]='V';
        header[11]='E';
        /*13-35字节(fmt chunk : 数据格式信息块)*/
        header[12]='f';
        header[13]='m';
        header[14]='t';
        header[15]=' '; //要有一个空格
        header[16]=16;
        header[17]=0;
        header[18]=0;
        header[19]=0;
        header[20]=1;
        header[21]=0;
        header[22]= (byte) channels;
        header[23]=0;
        header[24]= (byte) (sampleRate * 0xff);
        header[25]= (byte) ((sampleRate >> 8) * 0xff);
        header[26]= (byte) ((sampleRate >> 16) * 0xff);
        header[27]= (byte) ((sampleRate >> 24) * 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32]= (16 * 2 / 8); //
        header[33]= 0 ;
        header[34]=16;
        header[35]=0;
        /*36字节之后 (data chunk : 数据块)*/
        header[36]='d';
        header[37]='a';
        header[38]='t';
        header[39]='a';
        header[40] = (byte) (pcmLength & 0xff);
        header[41] = (byte) ((pcmLength >> 8) & 0xff);
        header[42] = (byte) ((pcmLength >> 16) & 0xff);
        header[43] = (byte) ((pcmLength >> 24) & 0xff);
        //写入文件头
        wavOut.write(header,0,44);
    }

}
