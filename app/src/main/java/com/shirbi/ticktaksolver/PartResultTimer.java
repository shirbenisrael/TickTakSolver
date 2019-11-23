package com.shirbi.ticktaksolver;

import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class PartResultTimer {
    MainActivity m_activity;
    TextView m_TextView;
    Timer m_timer;
    int m_countdown = 0;
    int direction;
    int current_alpha;

    public PartResultTimer(MainActivity activity, TextView textView) {
        m_activity = activity;
        m_TextView = textView;
    }

    public void StopAnimate()
    {
        m_countdown = 0;
    }

    public void Animate() {
        if (m_countdown != 0) {
            m_countdown = 20;
            // already running.
            return;
        }

        direction = 60;
        current_alpha = 0;
        m_countdown = 20;
        m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 100);
    }

    private void TimerMethod() {
        m_activity.runOnUiThread(m_timer_tick);
    }

    private Runnable m_timer_tick = new Runnable() {
        public void run() {
            if (m_countdown > 0) {
                m_countdown--;
                current_alpha += direction;
                if (current_alpha > 255) {
                    current_alpha = 255;
                    direction = -direction;
                }
                if (current_alpha < 0) {
                    current_alpha = 0;
                    direction = -direction;
                }
                m_TextView.setTextColor(m_TextView.getTextColors().withAlpha(current_alpha));
            } else {
                m_TextView.setTextColor(m_TextView.getTextColors().withAlpha(255));
                m_timer.cancel();
            }
        }
    };
}
