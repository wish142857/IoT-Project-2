package com.example.iot_project2;

public class Configuration {
    public static final int SamplingRate = 44100;
    public static final int StartFreq = 17000;
    public static final int EndFreq = 19000;
    public static final double T = 0.04;
    public static final int BandPassCenter = (StartFreq + EndFreq) / 2;
    public static final int BandPassOffset = EndFreq - StartFreq;
    public static final int StartThreshold = 20;
    public static final int SoundSpeed = 340;
    public static final int FMCW_FFTLen = 1024 * 64;

    public static final int SampleNum = 1 + (int)(T * SamplingRate);
}
