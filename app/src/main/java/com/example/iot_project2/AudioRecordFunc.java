package com.example.iot_project2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class AudioRecordFunc {
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    private String rawAudioName = "";       // 裸音频数据文件
    private String ripeAudioName = "";      // 可播放音频文件
    private AudioRecord audioRecord;
    private boolean isRecord = false;   // 设置正在录制的状态
    private static AudioRecordFunc mInstance;

    private AudioRecordFunc(){
    }

    /********************
     * 获取实例
     ********************/
    public synchronized static AudioRecordFunc getInstance() {
        if(mInstance == null)
            mInstance = new AudioRecordFunc();
        return mInstance;
    }

    /********************
     * 开始录音与文件储存
     ********************/
    public int startRecordAndFile() {
        //判断是否有外部存储设备 sdcard
        if(AudioFileFunc.isSdcardExit()) {
            if (isRecord)
                return ErrorCode.E_STATE_RECODING;
            if(audioRecord == null)
                creatAudioRecord();
            audioRecord.startRecording();
            isRecord = true;
            new Thread(new AudioRecordThread()).start();
            return ErrorCode.SUCCESS;
        }
        else {
            return ErrorCode.E_NOSDCARD;
        }
    }

    /********************
     * 停止录音与文件储存
     ********************/
    public void stopRecordAndFile() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            isRecord = false;//停止文件写入
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }

    /********************
     * 获取录音文件大小
     ********************/
    public long getRecordFileSize(){
        return AudioFileFunc.getFileSize(ripeAudioName);
    }

    /********************
     * 创建录音记录
     ********************/
    private void creatAudioRecord() {
        // 获取音频文件路径
        rawAudioName = AudioFileFunc.getRawFilePath();
        ripeAudioName = AudioFileFunc.getWavFilePath();

        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        // 创建AudioRecord对象
        audioRecord = new AudioRecord(AudioFileFunc.AUDIO_INPUT, AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);

    }

    /********************
     * 录音记录线程
     ********************/
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();                              // 往文件中写入裸数据
            formatWaveFile(rawAudioName, ripeAudioName);    // 格式化裸数据格式
        }
    }

    /********************
     * 将裸数据写入文件
     ********************/
    private void writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(rawAudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);   // 建立一个可存取字节的文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecord) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos!=null) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if(fos != null)
                fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /********************
     * 格式化 wav 格式文件
     ********************/
    private void formatWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE;
        int channels = 2;
        long byteRate = 16 * AudioFileFunc.AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /********************
     * 写入 wav 格式文件头
     ********************/
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate)
    throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}