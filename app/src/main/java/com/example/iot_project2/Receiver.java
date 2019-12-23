package com.example.iot_project2;

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
        return fmcw.delta_dis(input, start_pos);
    }

    public double[] xcorr(double[] input, double[] target) {
        double[] xcorr_result = new double[input.length];
        for (int i = 0; i < input.length; ++i) {
            xcorr_result[i] = 0;
            for (int j = 0; j < target.length && j < input.length - i; ++j) {
                xcorr_result[i] += input[i + j] * target[j];
            }
        }
        return xcorr_result;
    }

    public int find_start_position(double[] input) {
        double[] t = new double[Configuration.SampleNum];
        for (int i = 0; i < Configuration.SampleNum; i++) t[i] = ((double)i / Configuration.SamplingRate);
        double[] chirp = Chirp.chirp(t, Configuration.StartFreq, Configuration.T, Configuration.EndFreq);

        double[] xcorr_result = xcorr(input, chirp);

        double max = 0;
        int pos = -1;
        for (int i = 0; i < xcorr_result.length; i++) if (xcorr_result[i] > max) {
            max = xcorr_result[i];
            pos = i;
        }

        // Log.i("XCORR", String.format("max corr is: %.3f", max));
        if (max > Configuration.StartThreshold && pos >= 20) return pos - 20;
        else return -1;
    }
}
