package com.example.iot_project2;

import java.io.File;

import android.media.MediaRecorder;
import android.os.Environment;

public class AudioFileFunc {

    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;    //音频输入-麦克风
    public final static int AUDIO_SAMPLE_RATE = Configuration.SamplingRate;                      // 采样频率-44.1KHz
    //录音输出文件
    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";
    private final static String AUDIO_WAV_FILENAME = "FinalAudio.wav";
    private final static String RESULT_FILENAME = "Result.txt";


    /********************
     * 判断是否有外部存储设备 sdcard
     ********************/
    public static boolean isSdcardExit(){
        return (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED));
    }

    /********************
     * 获取 RawFile 路径
     ********************/
    public static String getRawFilePath(){
        String mAudioRawPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioRawPath = fileBasePath+"/"+AUDIO_RAW_FILENAME;
        }
        return mAudioRawPath;
    }

    /********************
     * 获取 WavFile 路径
     ********************/
    public static String getWavFilePath(){
        String mAudioWavPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioWavPath = fileBasePath+"/"+AUDIO_WAV_FILENAME;
        }
        return mAudioWavPath;
    }

    /********************
     * 获取 TxtFile 路径
     ********************/
    public static String getTxtFilePath(){
        String TxtPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            TxtPath = fileBasePath+"/"+RESULT_FILENAME;
        }
        return TxtPath;
    }

    /********************
     * 获取文件大小
     ********************/
    public static long getFileSize(String path){
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }

}