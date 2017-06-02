package org.song.demo.listvideo;

import android.view.View;

public interface CallBack {

    /**
     *
     */
    void setActive(View newActiveView, int position);

    /**
     *
     */
    void deactivate(View currentView, int position);
}