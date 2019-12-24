package com.example.iot_project2;

import android.util.Log;

public class Receiver {
    private BandPassFilter bandPassFilter;
    private FMCW fmcw;
    // private double[] filtered_sound;

    public Receiver() {
        bandPassFilter = new BandPassFilter(Configuration.BandPassCenter, Configuration.BandPassOffset, Configuration.SamplingRate);
        fmcw = new FMCW(Configuration.SamplingRate, Configuration.T, Configuration.StartFreq, Configuration.EndFreq);
    }

    public double[] convert_and_filter(byte[] buffer, int byte_num) {
        double[] doubles = new double[byte_num / 2];
        for (int i = 0; i < doubles.length; i++) {
            byte bl = buffer[2 * i];
            byte bh = buffer[2 * i + 1];

            short s = (short) ((bh & 0x00FF) << 8 | bl & 0x00FF);
            doubles[i] = s / 32768f;
        }
        return bandPassFilter.filter(doubles);
    }

    public double[] convert_and_filter(byte[] buffer) {
        return convert_and_filter(buffer, buffer.length);
    }

    public double calculate_distance(double[] input, int start_pos) {
        return fmcw.calculate_distance(input, start_pos);
    }


    /********************
     * xcorr 为matlab中的互相关函数，不同的是两段长度不一样
     ********************/
    public double[] xcorr(double[] input, double[] target) {
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
     * 利用xcorr 识别信号的开始位置
     ********************/
    public int find_start_position(double[] input) {
        double[] t = new double[Configuration.SampleNum];
        double sample_period = (double)1 / Configuration.SamplingRate;
        for (int i = 0; i < Configuration.SampleNum; i++) {
            t[i] = i * sample_period;
        }
        double[] chirp = Chirp.chirp(t, Configuration.StartFreq, Configuration.T, Configuration.EndFreq);

        double[] xcorr_result = xcorr(input, chirp);

        double max = 0;
        int pos = -1;
        for (int i = 0; i < xcorr_result.length; i++) {
            if (xcorr_result[i] > max) {
                max = xcorr_result[i];
                pos = i;
            }
        }

        Log.i("XCORR", String.format("max corr is: %.3f", max));
        if (max > Configuration.StartThreshold && pos >= 20) {
            return pos - 20;
        }
        else {
            return -1;
        }
    }
}
