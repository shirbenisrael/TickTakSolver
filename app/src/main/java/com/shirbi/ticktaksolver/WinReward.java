package com.shirbi.ticktaksolver;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class WinReward extends ImageView {
    MainActivity m_activity;
    private int m_inflate_center_x;
    private int m_inflate_center_y;
    private int m_base_diameter;
    private int m_diameter;
    private Boolean m_is_inflate;
    private float m_alpha;
    private Timer m_fade_out_timer;
    ViewGroup m_parent;

    public WinReward(Context context, ViewGroup parent, int center_x, int center_y, int diameter ) {
        super(context);
        m_activity = (MainActivity) context;

        m_parent = parent;
        m_inflate_center_x = center_x;
        m_inflate_center_y = center_y;
        m_base_diameter = diameter;

        setImageResource(R.drawable.trophy_icon);
    }

    public void SetParentView(ViewGroup newParent, RelativeLayout.LayoutParams params) {
        ViewParent oldParent = getParent();
        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(this);
        }

        newParent.addView(this, params);
    }

    public void RemoveFromParentView() {
        ((ViewGroup)(this.getParent())).removeView(this);
    }

    private void UpdateLocationWhenInflate() {
        RelativeLayout.LayoutParams token_params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        token_params.leftMargin = m_inflate_center_x - m_diameter / 2;
        token_params.topMargin = m_inflate_center_y - m_diameter / 2;

        SetParentView(m_parent, token_params);

        getLayoutParams().width = m_diameter;
        getLayoutParams().height = m_diameter;
    }

    public void Inflate() {
        m_is_inflate = true;
        m_diameter = m_base_diameter;

        UpdateLocationWhenInflate();

        FadeOut();
    }

    public void FadeOut() {
        setVisibility(VISIBLE);

        m_alpha = (float)1.0;
        m_fade_out_timer = new Timer();
        m_fade_out_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeOutTimerMethod();
            }

        }, 0, 50);
    }

    private void FadeOutTimerMethod() {
        if (m_alpha < 0.1) {
            m_fade_out_timer.cancel();
            return;
        }

        m_activity.runOnUiThread(m_fade_out_timer_tick);
    }

    private void SetAlphaOnImage() {
        setAlpha(m_alpha);
    }

    private Runnable m_fade_out_timer_tick = new Runnable() {
        public void run() {
            m_alpha -= 0.01;
            SetAlphaOnImage();

            if (m_is_inflate) {
                m_diameter += 9;
                UpdateLocationWhenInflate();
            }

            if (m_alpha < 0.1) {
                m_alpha = (float)1.0;
                m_fade_out_timer.cancel();
                setVisibility(INVISIBLE);
                SetAlphaOnImage();

                if (m_is_inflate) {
                    RemoveFromParentView();
                    m_is_inflate = false;
                }
            }
        }
    };
}
