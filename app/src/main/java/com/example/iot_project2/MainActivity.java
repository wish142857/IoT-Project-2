package com.example.iot_project2;

import java.util.ArrayList;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class MainActivity extends Activity {
    private final int isWaiting = -1;     // 正在等待
    private final int isRecording = 0;    // 正在录制
    private final int isPlaying = 1;      // 正在播放
    private int mState = isWaiting;       // 当前状态
    private TextView txt_info;
    private LineChart lineChart;
    private UIHandler uiHandler;
    private UIThread uiThread;

    private LineData lineData;           // 折线图数据
    private int index;                   // 折线图点索引

    /********************
     *  初始化创建
     ********************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 引用组件
        Button btn_record = this.findViewById(R.id.btn_record);
        Button btn_stop = this.findViewById(R.id.btn_stop);
        Button btn_play = this.findViewById(R.id.btn_play);
        txt_info = this.findViewById(R.id.txt_info);
        lineChart = findViewById(R.id.chart);
        // 设置监听
        btn_record.setOnClickListener(btn_record_clickListener);
        btn_stop.setOnClickListener(btn_stop_clickListener);
        btn_play.setOnClickListener(btn_stop_playListener);
        // 初始化
        initLineChart();
        uiHandler = new UIHandler();
        LineChartManager.uiHandler = uiHandler;
        // 权限申请
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    /********************
     * 录音按钮监听
     ********************/
    private Button.OnClickListener btn_record_clickListener = new Button.OnClickListener(){
        public void onClick(View v){
            LineChartManager.initLineChart();
            record();
        }
    };

    /********************
     * 停止按钮监听
     ********************/
    private Button.OnClickListener btn_stop_clickListener = new Button.OnClickListener(){
        public void onClick(View v){
            stop();
        }
    };

    /********************
     * 播放按钮监听
     ********************/
    private Button.OnClickListener btn_stop_playListener = new Button.OnClickListener(){
        public void onClick(View v){
            play();
        }
    };

    /********************
     * 开始录音
     ********************/
    private void record(){
        if (mState == isRecording){
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);
            uiHandler.sendMessage(msg);
            return;
        }
        if (mState == isPlaying) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_PLAY);
            msg.setData(b);
            uiHandler.sendMessage(msg);
            return;
        }
        AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
        int mResult = mRecord.startRecordAndFile();
        if(mResult == ErrorCode.SUCCESS){
            uiThread = new UIThread();
            new Thread(uiThread).start();
            mState = isRecording;
        } else {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);
            uiHandler.sendMessage(msg);
        }
    }

    /********************
     * 停止录音/播放
     ********************/
    private void stop(){
        if (mState == isRecording){
            AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
            mRecord.stopRecordAndFile();
            if(uiThread != null){
                uiThread.stopThread();
            }
            if ((uiHandler != null) && (uiThread != null))
                uiHandler.removeCallbacks(uiThread);
            Message msg = new Message();
            Bundle b = new Bundle();    // 存放数据
            b.putInt("cmd",CMD_STOP);
            b.putInt("msg", mState);
            msg.setData(b);
            uiHandler.sendMessageDelayed(msg,1000); // 向Handler发送消息,更新UI
            mState = isWaiting;
        }
        if (mState == isPlaying){
            AudioPlayFunc mPlay = AudioPlayFunc.getInstance();
            int mResult = mPlay.stopPlay();
            if (mResult == ErrorCode.SUCCESS){
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putInt("cmd",CMD_STOP);
                b.putInt("msg", mState);
                msg.setData(b);
                uiHandler.sendMessageDelayed(msg,200);
                mState = isWaiting;
            } else {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putInt("cmd",CMD_PLAYFAIL);
                b.putInt("msg", mResult);
                msg.setData(b);
                uiHandler.sendMessage(msg);
            }
        }
    }

    /********************
     * 播放录音
     ********************/
    private void play(){
        if (mState == isRecording){
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_PLAYFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);
            uiHandler.sendMessage(msg);
            return;
        }
        if (mState == isPlaying) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_PLAYFAIL);
            b.putInt("msg", ErrorCode.E_STATE_PLAY);
            msg.setData(b);
            uiHandler.sendMessage(msg);
            return;
        }
        AudioPlayFunc mPlay = AudioPlayFunc.getInstance();
        int mResult = mPlay.startPlay();
        if (mResult == ErrorCode.SUCCESS){
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_PLAY_TIME);
            b.putInt("msg", mResult);
            msg.setData(b);
            uiHandler.sendMessage(msg);
            mState = isPlaying;
        } else {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("cmd",CMD_PLAYFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);
            uiHandler.sendMessage(msg);
        }
    }

    /********************
     *  初始化折线图
     ********************/
    private void initLineChart() {
        Toast.makeText(MainActivity.this,"INIT" ,Toast.LENGTH_SHORT).show();
        lineChart.clear();
        ArrayList<Entry> list = new ArrayList<>();
        LineDataSet lineDataSet = new LineDataSet(list, "Label");
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
        index = 0;
    }

    /********************
     *  更新折线图
     ********************/
    public void updateLineChart(double movement) {
        Toast.makeText(MainActivity.this,"UPDATE" ,Toast.LENGTH_SHORT).show();
        lineData.addEntry(new Entry(index * 4 * (float)Global.T, (float) movement), 0);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
        index ++;
    }

    /********************
     * UI 处理
     ********************/
    public final static int CMD_RECORDING_TIME = 2000;
    public final static int CMD_PLAY_TIME = 2001;
    public final static int CMD_STOP = 2002;
    public final static int CMD_RECORDFAIL = 2003;
    public final static int CMD_PLAYFAIL = 2004;
    public final static int CMD_CHART_INIT = 2005;
    public final static int CMD_CHART_UPDATE = 2006;
    class UIHandler extends Handler {
        private UIHandler() {
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch(vCmd)
            {
                case CMD_RECORDING_TIME:
                    int vTime = b.getInt("msg");
                    MainActivity.this.txt_info.setText(String.format(getResources().getString(R.string.info_recording), vTime));
                    break;
                case CMD_PLAY_TIME:
                    AudioRecordFunc mRecord_ = AudioRecordFunc.getInstance();
                    long mSize_ = mRecord_.getRecordFileSize();
                    MainActivity.this.txt_info.setText(String.format(getResources().getString(R.string.info_playing), AudioFileFunc.getWavFilePath(), mSize_));
                    break;
                case CMD_STOP:
                    int vType = b.getInt("msg");
                    if (vType == isRecording) {
                        AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
                        long mSize = mRecord.getRecordFileSize();
                        MainActivity.this.txt_info.setText(String.format(getResources().getString(R.string.info_record_over), AudioFileFunc.getWavFilePath(), mSize));
                    } else if (vType == isPlaying) {
                        AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
                        long mSize = mRecord.getRecordFileSize();
                        MainActivity.this.txt_info.setText(String.format(getResources().getString(R.string.info_play_over), AudioFileFunc.getWavFilePath(), mSize));
                    }
                    break;
                case CMD_CHART_INIT:
                    initLineChart();
                    break;
                case CMD_CHART_UPDATE:
                    double vMovement = b.getDouble("msg");
                    updateLineChart(vMovement);
                    break;
                default:
                    int vErrorCode = b.getInt("msg");
                    String vMsg = ErrorCode.getErrorInfo(MainActivity. this, vErrorCode);
                    Toast.makeText(MainActivity.this,vMsg ,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /********************
     * 计时线程
     ********************/
    class UIThread implements Runnable {
        int mTimeMill = 0;
        boolean vRun = true;
        private void stopThread(){
            vRun = false;
        }
        public void run() {
            while(vRun){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mTimeMill ++;
                Log.d("thread", "mThread........"+mTimeMill);
                Message msg = new Message();
                Bundle b = new Bundle();    // 存放数据
                b.putInt("cmd",CMD_RECORDING_TIME);
                b.putInt("msg", mTimeMill);
                msg.setData(b);
                MainActivity.this.uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }
        }
    }

}