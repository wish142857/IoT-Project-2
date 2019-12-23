package com.example.iot_project2;

import java.io.IOException;

import android.media.MediaPlayer;
import android.util.Log;


public class AudioPlayFunc {

    private String rawAudioName = "";       // 裸音频数据文件名
    private String ripeAudioName = "";      // 可播放音频文件名
    private boolean isPlay = false;       // 是否正在播放
    private MediaPlayer mediaPlayer;
    private static AudioPlayFunc mInstance;
    private Transmitter transmitter;

    /********************
     * 构造方法
     ********************/
    private AudioPlayFunc(){
        transmitter = new Transmitter();
    }

    /********************
     * 获取实例
     ********************/
    public synchronized static AudioPlayFunc getInstance() {
        if(mInstance == null)
            mInstance = new AudioPlayFunc();
        return mInstance;
    }

    /********************
     * 加载音频文件
     ********************/
    private void loadAudioFile() {
        transmitter.write_result(AudioFileFunc.getWavFilePath());
        // 获取音频文件路径
        ripeAudioName = AudioFileFunc.getWavFilePath();
        // 设置播放数据源
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(ripeAudioName);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /********************
     * 开始播放
     ********************/
    public int startPlay() {
        if(AudioFileFunc.isSdcardExit()) {
            if (isPlay)
                return ErrorCode.E_STATE_RECODING;
            loadAudioFile();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);
                }
            });
            isPlay = true;
            return ErrorCode.SUCCESS;
        }
        else {
            return ErrorCode.E_NOSDCARD;
        }
    }

    /********************
     * 停止播放
     ********************/
    public int stopPlay() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        isPlay = false;
        return ErrorCode.SUCCESS;
    }
}
