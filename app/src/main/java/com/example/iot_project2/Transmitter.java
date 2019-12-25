package com.example.iot_project2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;

public class Transmitter {
    int sample_num = Configuration.SampleNum;
    int fs = Configuration.SamplingRate;
    int f0 = Configuration.StartFreq;
    int f1 = Configuration.EndFreq;
    double T = Configuration.T;
    int chirp_num = 1000;


    private byte[] generate_signal() {
        double[] t = new double[sample_num];

        // 对应 t = [0:1/fs:T];
        double sample_period = (double)1 / fs;
        for (int i = 0; i < sample_num; i++) {
            t[i] = i * sample_period;
        }

        // 对应 data = chirp(t, f0, T, f1, 'linear');
        double[] chirp = Chirp.chirp(t, f0, T, f1);

        // 对应
        // output = [];
        // for i = 1:chirp_num
        //     output = [output, data, zeros(1,sample_num)];
        // end
        double[] message = new double[sample_num * 2 * chirp_num];
        for (int i = 0; i < chirp_num; i++) {

            // chirp信号
            for (int j = 0; j < sample_num; j++) {
                message[i * 2 * sample_num + j] = chirp[j];
            }

            // chirp信号后的空白
            for (int j = sample_num; j < 2 * sample_num; j++) {
                message[i * 2 * sample_num + j] = 0;
            }
        }
        return DoubleByteConvert.double2byte(message, message.length);
    }

    public void write_signal_to_file(String file_name) {
        File file = new File(file_name);
        if (file.exists()) {
            file.delete();
        }
        try { file.createNewFile(); } catch (IOException e){
            throw new IllegalStateException("unable to create " + file.toString());
        }

        byte[] input = generate_signal();

        int channels = 1;  // 单声道

        //每分钟录到的数据的字节数
        long byteRate = 2 * fs * channels;


        try {
            FileOutputStream os = new FileOutputStream(file);
            int audio_len = input.length;
            AudioRecordFunc.WriteWaveFileHeader(os, audio_len, audio_len + 36, fs, channels, byteRate);
            os.write(input);
            os.close();
        } catch (Throwable t){
            Log.e("MainActivity", "failed to write");
        }
    }


}
