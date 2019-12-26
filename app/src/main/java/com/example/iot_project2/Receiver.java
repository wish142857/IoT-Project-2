package com.example.iot_project2;

import android.util.Log;


/********************
 * 将滤波、找开始位置、计算距离封装到一个类中，将在录音函数中使用
 ********************/
public class Receiver {
    private int sample_rate;
    private double T;
    private int start_freq;
    private int end_freq;
    private int xcorr_thresh;
    private int sample_num;
    private BandPassFilter band_pass_filter;
    private FMCW fmcw;

    public Receiver(int sample_rate_, int start_freq_, int end_freq_, int freq_center_,
                    int freq_offset_, double T_, int xcorr_thresh_, int sample_num_) {
        sample_rate = sample_rate_;
        T = T_;
        start_freq = start_freq_;
        end_freq = end_freq_;
        xcorr_thresh = xcorr_thresh_;
        sample_num = sample_num_;
        band_pass_filter = new BandPassFilter(freq_center_, freq_offset_, sample_rate);
        fmcw = new FMCW(sample_rate, start_freq, end_freq, T);
    }

    /********************
     * 由raw的音频数据转化为double类型再滤波
     ********************/
    public double[] convert_and_filter(byte[] buffer, int byte_num) {

        return band_pass_filter.filter(DoubleByteConvert.byte2double(buffer, byte_num));
    }

    /********************
     * 由FMCW机制计算距离
     ********************/
    public double calculate_distance(double[] input, int start_pos) {
        return fmcw.calculate_distance(input, start_pos);
    }


    /********************
     * xcorr为matlab中的互相关函数，不同的是两段长度不一样
     ********************/
    private double[] calculate_xcorr(double[] input, double[] target) {
        int input_length = input.length;
        double[] xcorr_result = new double[input_length];
        for (int i = 0; i < input_length; ++i) {
            xcorr_result[i] = 0;
            for (int j = 0; j < target.length && j < input_length - i; ++j) {
                xcorr_result[i] += input[i + j] * target[j];
            }
        }
        return xcorr_result;
    }

    /********************
     * 利用xcorr识别信号的开始位置
     ********************/
    public int find_start_position(double[] input) {
        double[] t = new double[sample_num];
        double sample_period = (double)1 / sample_rate;
        for (int i = 0; i < sample_num; i++) {
            t[i] = i * sample_period;
        }
        double[] chirp = Chirp.chirp(t, start_freq, T, end_freq);

        double[] xcorr = calculate_xcorr(input, chirp);

        double max_xcorr = 0;
        int pos = -1;
        for (int i = 0; i < xcorr.length; i++) {
            if (xcorr[i] > max_xcorr) {
                max_xcorr = xcorr[i];
                pos = i;
            }
        }

        Log.i("xcorr", String.format("%.4f", max_xcorr));

        // 25是一个经验值，
        if (max_xcorr > xcorr_thresh && pos >= 25) {
            return pos - 25;
        }
        else {
            return -1;
        }
    }
}
