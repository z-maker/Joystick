package com.iron.joystick;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.iron.library.Joystick;
import com.iron.library.JoystickActionListener;

import java.util.Locale;

public class ControlPad extends AppCompatActivity {


    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_jpad);

        textView = findViewById(R.id.txtDirection);

        Joystick joystick = findViewById(R.id.joystickA);

        joystick.setJoystickActionListener(
                (View stickView, int direction, float angle, float x_axis, float y_axis) -> {

                String directionName = "";

                switch (direction){
                    case Joystick.STICK_UP:
                        directionName=("UP");
                        break;
                    case Joystick.STICK_UP_RIGHT:
                        directionName=("UP_RIGHT");
                        break;
                    case Joystick.STICK_RIGHT:
                        directionName=("RIGHT");
                        break;
                    case Joystick.STICK_DOWN_RIGHT:
                        directionName=("DOWN_RIGHT");
                        break;
                    case Joystick.STICK_DOWN:
                        directionName=("DOWN");
                        break;
                    case Joystick.STICK_DOWN_LEFT:
                        directionName=("DOWN_LEFT");
                        break;
                    case Joystick.STICK_LEFT:
                        directionName=("LEFT");
                        break;
                    case Joystick.STICK_UP_LEFT:
                        directionName=("UP_LEFT");
                        break;
                    case Joystick.STICK_NONE:
                        directionName=("NONE");
                        break;
                }

                String text = String.format(Locale.getDefault(),
                        "Direction: %s \nAngle: %f \nAxisX: %f \nAxisY: %f",
                        directionName,angle,x_axis,y_axis);

                textView.setText(text);

            });
    }


}
