package com.example.iot_project2;

public class BandPassFilter {
    private int center_freq;
    private int offset_freq;
    private int sample_freq;
    // 滤波器阶数
    private int M;
    private double[] h;

    public BandPassFilter(int centerFreq, int offsetFreq, int sampleFreq) {
        this.center_freq = centerFreq;
        this.offset_freq = offsetFreq;
        this.sample_freq = sampleFreq;
        double AP = 0.82; // 通带衰减
        double As = 45.0; // 阻带衰减
        double Wp1 = 2 * Math.PI * (center_freq - offset_freq) / sample_freq;
        double Wp2 = 2 * Math.PI * (center_freq + offset_freq) / sample_freq;

        int N = (int)Math.ceil(3.6 * sample_freq / offset_freq);
        M = N - 1;
        M += (M % 2);

        h = new double[M + 1];
        for (int k = 0; k <= M; k++) {
            if (k - M / 2 == 0) h[k] = (Wp2 - Wp1) / Math.PI;
            else h[k] = Wp2 * Math.sin(Wp2 * (k - M / 2)) / (Math.PI * (Wp2 * (k - M / 2))) -
                    Wp1 * Math.sin(Wp1 * (k - M / 2)) / (Math.PI * (Wp1 * (k - M / 2)));
        }
    }

    public double[] filter(double[] input) {
        // The filter in band pass uses matlab filter as `filter(h, 1, input)`
        int len = input.length;
        double[] output = new double[len];
        for (int i = 0; i < len; i++) {
            double y_front = 0;
            for(int j = 0; j <= M && j <= i; j++)
                y_front += h[j] * input[i - j];
            output[i] = y_front;
        }
        return output;
    }
}
