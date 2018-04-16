package com.iron.library;

import android.view.View;

public interface JoystickActionListener {
    void onMotionDetection(View joystick,int direction,float angle,float x_axis,float y_axis);
}
