package com.example.iot_project2;


/********************
 * 全局变量，主要是控制FMCW算法的一些参数
 ********************/
public class Global {
    public static final int SamplingRate = 44100; // 采样率
    public static final int StartFreq = 17000; // 开始频率
    public static final int EndFreq = 19000; // 结束频率
    public static final double T = 0.04; // 周期
    public static final int BandPassCenter = (StartFreq + EndFreq) / 2; // 带通滤波的中心频率
    public static final int BandPassOffset = EndFreq - StartFreq; // 带通滤波中心频率到两端的差值（设置带通滤波的范围是FMCW频率范围的两倍）
    public static final int XcorrThresh = 50; // 互相关值多大时判定为信号开始
    public static final int SoundSpeed = 340; // 声速，取340
    public static final int FFTLen = 1024 * 64; // 取参考代码中相同的值
    public static final int SampleNum = 1 + (int)(T * SamplingRate); // chirp及其后空白信号长度之和，与参考代码中的值一致
}