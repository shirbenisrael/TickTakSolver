package com.shirbi.ticktaksolver;

public class FrontEndHandler {
    MainActivity m_activity;

    public FrontEndHandler(MainActivity activity) {
        m_activity = activity;

        m_activity.setContentView(R.layout.activity_main);

    }
}
