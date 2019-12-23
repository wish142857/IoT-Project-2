package com.example.iot_project2;

import android.os.Bundle;
import android.os.Message;

public class LineChartManager {
    static MainActivity.UIHandler uiHandler;
    private final static int CMD_CHART_UPDATE = 2005;

    static boolean initLineChart() {
        if (uiHandler == null)
            return false;
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt("cmd",CMD_CHART_UPDATE);
        b.putInt("msg", 0);
        msg.setData(b);
        uiHandler.sendMessage(msg);
        return true;
    }
}
