package com.example.iot_project2;

import android.os.Bundle;
import android.os.Message;

public class LineChartManager {
    static MainActivity.UIHandler uiHandler;

    static void initLineChart() {
        if (uiHandler == null)
            return;
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt("cmd",MainActivity.CMD_CHART_INIT);
        b.putInt("msg", 0);
        msg.setData(b);
        uiHandler.sendMessage(msg);
    }

    static void updateLineChart() {
        if (uiHandler == null)
            return;
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt("cmd",MainActivity.CMD_CHART_UPDATE);
        b.putInt("msg", 0);
        msg.setData(b);
        uiHandler.sendMessage(msg);
    }
}
