package cn.garymb.ygomobile.core;

import android.annotation.SuppressLint;
import android.app.NativeActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import cn.garymb.ygomobile.controller.InputQueueCompat;

public abstract class GameActivity extends NativeActivity {
    protected FrameLayout mLayout;
    protected SurfaceView mSurfaceView;
    private boolean replaced = false;
    //自定义surface，方便控制窗口大小
    protected static final boolean USE_SURFACE = true;
    //接管系统触摸事件，再次分发给游戏
    protected static final boolean USE_MY_INPUT = true;
    private InputQueueCompat inputQueueCompat;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        if (USE_SURFACE) {
            mSurfaceView = new SurfaceView(this);
        }
        if (USE_MY_INPUT) {
            inputQueueCompat = new InputQueueCompat();
            if (!inputQueueCompat.isValid()) {
                inputQueueCompat = null;
            }
        }
        initBeforeOnCreate();
        super.onCreate(savedInstanceState);
        initAfterOnCreate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (inputQueueCompat != null) {
                super.onInputQueueCreated(inputQueueCompat.getInputQueue());
            }
        } else {
            if (inputQueueCompat != null) {
                super.onInputQueueDestroyed(inputQueueCompat.getInputQueue());
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setContentView(View view) {
        Size size = getGameWindowSize();
        mLayout = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size.getWidth(), size.getHeight());
//        mLayout.setBackgroundColor(Color.BLACK);
        lp.gravity = Gravity.CENTER;
        if (USE_SURFACE) {
            mLayout.addView(mSurfaceView, lp);
            mLayout.addView(view, lp);
            super.setContentView(mLayout);
//            app().attachGame(this);
//            changeGameSize();
            getWindow().takeSurface(null);
            if (USE_MY_INPUT && inputQueueCompat != null) {
                getWindow().takeInputQueue(null);
            }
            replaced = true;
            mSurfaceView.getHolder().addCallback(this);
            mSurfaceView.requestFocus();
            getWindow().setGravity(Gravity.CENTER);
            if (USE_MY_INPUT && inputQueueCompat != null) {
                Log.d(IrrlichtBridge.TAG, "use java input queue:" + inputQueueCompat.getNativePtr());
                mSurfaceView.setOnTouchListener((v, event) -> {
                    onSurfaceTouch(v, event);
                    return true;
                });
            }
        } else {
            mLayout.addView(view, lp);
            getWindow().setGravity(Gravity.CENTER);
            super.setContentView(mLayout);
        }
    }

    protected void onSurfaceTouch(View v, MotionEvent event){
        sendInputEvent(event, false);
    }

    protected void sendInputEvent(InputEvent event, boolean predispatch){
        if (inputQueueCompat != null) {
            inputQueueCompat.sendInputEvent(event, this, predispatch);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (USE_SURFACE) {
            if (!replaced) {
                return;
            }
        }
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (USE_SURFACE) {
            if (!replaced) {
                return;
            }
        }
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (USE_SURFACE) {
            if (!replaced) {
                return;
            }
        }
        super.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        if (USE_SURFACE) {
            if (!replaced) {
                return;
            }
        }
        super.surfaceRedrawNeeded(holder);
    }

    protected abstract Size getGameWindowSize();
    protected abstract void initBeforeOnCreate();
    protected abstract void initAfterOnCreate();
}
