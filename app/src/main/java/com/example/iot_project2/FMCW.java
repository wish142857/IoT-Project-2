package com.example.iot_project2;


/********************
 * FMCW算法计算距离的核心部分
 ********************/
public class FMCW {
    private int fs;
    private double T;
    private int f0;
    private int f1;
    private int sample_num;
    private double[] pseudo_T;
    private int fft_len = Global.FFTLen;
    private int c = Global.SoundSpeed;
    private int start_idx = 0;


    // 1-7 行
    public FMCW(int sample_rate, int start_freq, int end_freq, double T_) {
        fs = sample_rate;
        T = T_;
        f0 = start_freq;
        f1 = end_freq;
        sample_num = (int)(T * fs) + 1;
        double[] t = new double[sample_num];
        double sample_period = (double)1 / fs;
        for (int i = 0; i < sample_num; i++) {
            t[i] = i * sample_period;
        }
        pseudo_T = Chirp.chirp(t, f0, T, f1);
    }

    // 30-34 行
    public double calculate_distance(double[] input, int start) {
        if (input.length - start < sample_num)
            throw new RuntimeException("input data length wrong!");

        // 对应 s = pseudo_T.*mydata';
        Complex[] s = new Complex[fft_len];
        for (int i = 0; i < fft_len && i < sample_num; i++)
            s[i] = new Complex(pseudo_T[i] * input[start + i], 0);
        for (int i = sample_num; i < fft_len; i++)
            s[i] = new Complex(0, 0);

        // 对应 FFT_out = abs(fft(s(i:i+len/2),fftlen));
        Complex[] FFT_out = FastFourierTransform.fft(s);


        // 对应
        // [~, idx] = max(abs(FFT_out(1:round(fftlen/2))));
        // idxs(round((i-start)/len)+1) = idx;
        double max_fft = 0;
        int idx = -1;
        for (int i = 0; i < fft_len / 2; i++){
            double fft = FFT_out[i].abs();
            if (fft > max_fft) {
                max_fft = fft;
                idx = i;
            }
        }

        // 对应
        // start_idx = 0;
        // delta_distance = (idxs-start_idx)*fs/fftlen*340*T/(f1-f0);
        return (idx - start_idx) * T * c / (f1 - f0) / fft_len * fs ;
    }

}
