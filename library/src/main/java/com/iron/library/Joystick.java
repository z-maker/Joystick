package com.iron.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.iron.library.R.drawable;


@SuppressWarnings("unused")
public class Joystick extends View implements View.OnTouchListener {

    public static final int STICK_NONE = 0;
    public static final int STICK_UP = 1;
    public static final int STICK_UP_RIGHT = 2;
    public static final int STICK_RIGHT = 3;
    public static final int STICK_DOWN_RIGHT = 4;
    public static final int STICK_DOWN = 5;
    public static final int STICK_DOWN_LEFT = 6;
    public static final int STICK_LEFT = 7;
    public static final int STICK_UP_LEFT = 8;


    private int stickAlpha = 255;
    private int stickBgAlpha = 255;
    private Paint stick_paint;
    private Paint background_paint;

    private Bitmap stick;
    private Bitmap stick_bg;

    private int stick_size = 100;
    private int stick_bg_size = 200;
    private int view_size;

    private int offset = 50;
    private int stick_offset = 50;
    private int min_distance = 0;
    private boolean touch_state = false;
    private int view_center_x;
    private int view_center_y;
    private boolean isFullAxis = true;
    private boolean invertY = true;

    //to retrieve on inferface
    private float distance = 0;
    private float angle = 0;
    private int pointer_pos_x = 0;
    private int pointer_pos_y = 0;
    private int stick_pos_x = 0;
    private int stick_pos_y = 0;

    private JoystickActionListener joystickActionListener;

    public Joystick(Context context) {
        super(context);
        init(context, null);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {

            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.Joystick);

            min_distance = array.getInt(R.styleable.Joystick_min_distance, min_distance);
            offset = array.getInt(R.styleable.Joystick_offset,0);
            isFullAxis = array.getBoolean(R.styleable.Joystick_full_axis, isFullAxis);
            invertY = array.getBoolean(R.styleable.Joystick_invert_y, invertY);

            stick_size = array.getInt(R.styleable.Joystick_stick_size, stick_size);
            stickAlpha = array.getInt(R.styleable.Joystick_stick_alpha, stickAlpha);
            stickBgAlpha = array.getInt(R.styleable.Joystick_stick_bg_alpha, stickBgAlpha);

            stick = BitmapFactory.decodeResource(context.getResources(),
                    array.getResourceId(R.styleable.Joystick_stick,
                            R.drawable.stick_a));
            stick_bg = BitmapFactory.decodeResource(context.getResources(),
                    array.getResourceId(R.styleable.Joystick_stick_bg,
                            drawable.stick_a));

            array.recycle();

        } else {
            stick_bg = BitmapFactory.decodeResource(context.getResources(), drawable.stick_a);
            stick = BitmapFactory.decodeResource(context.getResources(), drawable.stick_a);
        }


        stick_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        background_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stick_paint.setAntiAlias(true);
        background_paint.setAntiAlias(true);

        stick_paint.setAlpha(stickAlpha);
        background_paint.setAlpha(stickBgAlpha);

        setOnTouchListener(this);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (!changed)
            return;

        int d = Math.min(getMeasuredWidth(), getMeasuredHeight());

        view_size = d / 2;
        view_center_x = d / 2;
        view_center_y = d / 2;

        pointer_pos_x = view_center_x;
        pointer_pos_y = view_center_y;

        stick_bg_size = d;

        setUpBackground();
        setUpStick();

        stick_pos_x = pointer_pos_x - stick_offset;
        stick_pos_y = pointer_pos_y - stick_offset;

        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(stick_bg, view_center_x - (stick_bg_size / 2), view_center_y - (stick_bg_size / 2), background_paint);
        canvas.drawBitmap(stick, stick_pos_x, stick_pos_y, stick_paint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        drawStick(event);

        if (joystickActionListener ==null)
            return true;

        if (touch_state){
            joystickActionListener.onMotionDetection(this,getDirection(),getAngle(),getAxisX(),getAxisY());
        }else {
            joystickActionListener.onMotionDetection(this,0,0,0,0);
        }

        return true;
    }

    public void drawStick(MotionEvent event) {
        pointer_pos_x = (int) (event.getX() - (getWidth() / 2));
        pointer_pos_y = (int) (event.getY() - (getHeight() / 2));

        distance = (float) Math.hypot(pointer_pos_x, pointer_pos_y);

        angle = (float) angle(pointer_pos_x, pointer_pos_y);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (view_size) - offset) {
                //draw.position(event.getX(), event.getY());
                stick_pos_x = (int)event.getX() - stick_offset;
                stick_pos_y = (int)event.getY() - stick_offset;
                touch_state = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && touch_state) {
            if (distance <= (view_size) - offset) {
                stick_pos_x = (int)event.getX() -stick_offset;
                stick_pos_y = (int)event.getY() -stick_offset;
                //draw.position(event.getX(), event.getY());
            } else if (distance > (view_size) - offset) {
                float x = (float) (Math.cos(Math.toRadians(angle(pointer_pos_x, pointer_pos_y))) * ((view_size) - offset));
                float y = (float) (Math.sin(Math.toRadians(angle(pointer_pos_x, pointer_pos_y))) * ((view_size) - offset));
                x += (view_size);
                y += (view_size);
                //draw.position(x, y);
                stick_pos_x = (int) x - stick_offset;
                stick_pos_y = (int) y - stick_offset;

            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //draw.reset();
            stick_pos_x = view_center_x-stick_offset;
            stick_pos_y = view_center_y-stick_offset;
            touch_state = false;

        }
        invalidate();
    }

    private void setUpBackground() {
        stick_bg = Bitmap.createScaledBitmap(stick_bg, stick_bg_size, stick_bg_size, true);
    }

    private void setUpStick() {
        stick = Bitmap.createScaledBitmap(stick, stick_size, stick_size, true);
        stick_size = stick.getWidth();
        stick_offset = stick_size / 2;
        if (offset<=0)offset=stick_offset;
    }

    private double angle(float x, float y) {
        if (x >= 0 && y >= 0)// ++
            return Math.toDegrees(Math.atan(y / x));
        else if (x < 0 && y >= 0)//-+
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x < 0 && y < 0)//--
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x >= 0 && y < 0)//+-
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    private int get4Direction() {
        if (distance > min_distance && touch_state) {
            if (angle >= 225 && angle < 315) {
                return STICK_UP;
            } else if (angle >= 315 || angle < 45) {
                return STICK_RIGHT;
            } else if (angle >= 45 && angle < 135) {
                return STICK_DOWN;
            } else if (angle >= 135 && angle < 225) {
                return STICK_LEFT;
            }
        } else if (distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    private int get8Direction() {
        if (distance > min_distance && touch_state) {
            if (angle >= 247.5 && angle < 292.5) {
                return STICK_UP;
            } else if (angle >= 292.5 && angle < 337.5) {
                return STICK_UP_RIGHT;
            } else if (angle >= 337.5 || angle < 22.5) {
                return STICK_RIGHT;
            } else if (angle >= 22.5 && angle < 67.5) {
                return STICK_DOWN_RIGHT;
            } else if (angle >= 67.5 && angle < 112.5) {
                return STICK_DOWN;
            } else if (angle >= 112.5 && angle < 157.5) {
                return STICK_DOWN_LEFT;
            } else if (angle >= 157.5 && angle < 202.5) {
                return STICK_LEFT;
            } else if (angle >= 202.5 && angle < 247.5) {
                return STICK_UP_LEFT;
            }
        } else if (distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    private float getDistance() {
        /*if (distance > min_distance && distance <= ((view_size) - offset) && touch_state) {
            return distance;
        }
        return ((view_size) - offset);
        */

        int full = ((view_size) - offset);

        if (distance > min_distance && distance <= ((view_size) - offset) && touch_state) {

            return ((distance * 100)/full)/100;
        }
        return 1;
    }


    public int getDirection() {
        if (!isFullAxis)
            return get4Direction();
        else
            return get8Direction();
    }

    public int getJX() {
        if (distance > min_distance && distance <= ((view_size) - offset) && touch_state) {
            return pointer_pos_x;
        } else {
            return (int) (Math.cos(Math.toRadians(angle(pointer_pos_x, pointer_pos_y))) * ((view_size) + offset));
        }
    }

    public int getJY() {
        if (distance > min_distance && distance <= ((view_size) - offset) && touch_state) {
            if (invertY) {
                return -pointer_pos_y;
            } else {
                return pointer_pos_y;
            }
        } else {
            if (invertY) {
                return (int) -(Math.sin(Math.toRadians(angle(pointer_pos_x, pointer_pos_y))) * ((view_size) + offset));
            } else {
                return (int) (Math.sin(Math.toRadians(angle(pointer_pos_x, pointer_pos_y))) * ((view_size) + offset));
            }
        }
    }

    public float getAxisX(){
        if (getJX()>0){
            return getDistance();
        }else {
            return -getDistance();
        }
    }

    public float getAxisY(){

        if (getJY()>0){
            return getDistance();
        }else {
            return -getDistance();
        }
    }

    public float getAngle() {
        if (distance > min_distance && touch_state) {
            return angle;
        }
        return 0;
    }

    public int[] getPosition() {
        if (distance > min_distance && touch_state) {
            return new int[]{pointer_pos_x, pointer_pos_y};
        }
        return new int[]{0, 0};
    }

    public float[] getAxes() {
        if (distance > min_distance && touch_state) {
            return new float[]{getAxisX(),getAxisY()};
        }
        return new float[]{0, 0};
    }




    //geters & seters
    public void setJoystickActionListener(JoystickActionListener joystickActionListener) {
        this.joystickActionListener = joystickActionListener;
    }

    public JoystickActionListener getJoystickActionListener() {
        return joystickActionListener;
    }

    public int getStickAlpha() {
        return stickAlpha;
    }

    public void setStickAlpha(int stickAlpha) {
        this.stickAlpha = stickAlpha;
    }

    public int getStickBgAlpha() {
        return stickBgAlpha;
    }

    public void setStickBgAlpha(int stickBgAlpha) {
        this.stickBgAlpha = stickBgAlpha;
    }

    public Bitmap getStick() {
        return stick;
    }

    public void setStick(Bitmap stick) {
        this.stick = stick;
    }

    public Bitmap getStick_bg() {
        return stick_bg;
    }

    public void setStick_bg(Bitmap stick_bg) {
        this.stick_bg = stick_bg;
    }

    public int getStick_size() {
        return stick_size;
    }

    public void setStick_size(int stick_size) {
        this.stick_size = stick_size;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getStick_offset() {
        return stick_offset;
    }

    public void setStick_offset(int stick_offset) {
        this.stick_offset = stick_offset;
    }

    public int getMin_distance() {
        return min_distance;
    }

    public void setMin_distance(int min_distance) {
        this.min_distance = min_distance;
    }

    public boolean isFullAxis() {
        return isFullAxis;
    }

    public void isFullAxis(boolean fullAxis) {
        this.isFullAxis = fullAxis;
    }

    public boolean isInvertY() {
        return invertY;
    }

    public void invertY(boolean invertY) {
        this.invertY = invertY;
    }


}
