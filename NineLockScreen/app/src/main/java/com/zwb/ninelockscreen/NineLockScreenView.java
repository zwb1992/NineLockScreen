package com.zwb.ninelockscreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwb
 * Description
 * Date 2017/6/1.
 */

public class NineLockScreenView extends View {
    private int radius = 60;
    private List<Point> points = new ArrayList<>();//所有点
    private List<Point> selectedPoints = new ArrayList<>();//被选中的所有点
    private boolean init = false;//是否已经初始化了
    private Paint pointPaint;
    private Paint linePaint;
    private float lineWidth = 10;//线宽
    private Path path;//线的路径
    private boolean isError;//密码是否错误--发生在手指抬起的时候
    private float moveX, moveY;//触摸点位置
    private boolean showingResult = false;//是否正在显示结果，这时候不处理触摸事件
    private String password;
    private OnCallBack onCallBack;

    private int normalColor;
    private int selectedColor;
    private int errorColor;

    public void setOnCallBack(OnCallBack onCallBack) {
        this.onCallBack = onCallBack;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public NineLockScreenView(Context context) {
        this(context, null);
    }

    public NineLockScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineLockScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NineLockScreenView, defStyleAttr, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.NineLockScreenView_normal_color:
                    normalColor = array.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.NineLockScreenView_selected_color:
                    selectedColor = array.getColor(attr, Color.GRAY);
                    break;
                case R.styleable.NineLockScreenView_error_color:
                    errorColor = array.getColor(attr, Color.RED);
                    break;
                case R.styleable.NineLockScreenView_point_radius:
                    radius = (int) array.getDimension(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.NineLockScreenView_line_width:
                    lineWidth = array.getDimension(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
                    break;
            }
        }
        array.recycle();

        pointPaint = new Paint();
        pointPaint.setDither(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(lineWidth);

        path = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(sizeWidth, sizeHeight);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!init) {
            init = true;
            //初始化一个3*3的圆点
            float x;
            float y;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == 0) {//第一行
                        y = radius + getPaddingTop();
                    } else if (i == 1) {//第二行
                        y = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2.0f + getPaddingTop();
                    } else {//第三行
                        y = (getMeasuredHeight() - getPaddingBottom()) - radius;
                    }
                    if (j == 0) {//第一列
                        x = getPaddingLeft() + radius;
                    } else if (j == 1) {//第二列
                        x = (getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) / 2.0f + getPaddingLeft();
                    } else {//第三列
                        x = getMeasuredWidth() - getPaddingRight() - radius;
                    }
                    Point point = new Point(x, y, radius, i * 3 + j);
                    points.add(point);
                }
            }
        }

        drawPath(canvas);
        drawPoints(canvas);
    }

    /**
     * 画线
     *
     * @param canvas 画布
     */
    private void drawPath(Canvas canvas) {
        if (isError) {
            linePaint.setColor(errorColor);
        } else {
            linePaint.setColor(selectedColor);
        }
        path.reset();
        for (int i = 0; i < selectedPoints.size(); i++) {
            Point point = selectedPoints.get(i);
            if (i == 0) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }
        //在有选中点的情况下，才连接最后的触摸点，否则不划线
        if (!selectedPoints.isEmpty()) {
            path.lineTo(moveX, moveY);
        }
        canvas.drawPath(path, linePaint);
    }

    /**
     * 画圆点
     *
     * @param canvas 画布
     */
    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (point.getState() == Point.STATE.NORMAL) {
                pointPaint.setColor(normalColor);
            } else if (point.getState() == Point.STATE.SELECTED) {
                pointPaint.setColor(selectedColor);
            } else {
                pointPaint.setColor(errorColor);
            }
            canvas.drawCircle(point.getX(), point.getY(), point.getRadius(), pointPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showingResult) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("info", "11111111111111");
                break;
            case MotionEvent.ACTION_MOVE:
                handleAtMove(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //如果被选中的点不为空，触摸的x，y为最后一个点的位置
                if (!selectedPoints.isEmpty()) {
                    moveX = selectedPoints.get(selectedPoints.size() - 1).getX();
                    moveY = selectedPoints.get(selectedPoints.size() - 1).getY();
                    showingResult = true;
                    handleResult();
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
                break;
        }
        invalidate();
        return true;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            reset();
            postInvalidate();
        }
    };

    /**
     * 处理移动事件
     *
     * @param event 事件
     */
    private void handleAtMove(MotionEvent event) {
        moveX = event.getX();
        moveY = event.getY();
        Point point = checkPoint(moveX, moveY);
        //触摸的点在圆内且之前没有被选中
        if (point != null && !selectedPoints.contains(point)) {
            point.setState(Point.STATE.SELECTED);
            selectedPoints.add(point);
        }
    }

    /**
     * 检测触摸点是否在圆内
     *
     * @return 触摸点
     */
    private Point checkPoint(float x, float y) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (point.contains(x, y)) {
                return point;
            }
        }
        return null;
    }

    /**
     * 初始化状态
     */
    private void reset() {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            point.setState(Point.STATE.NORMAL);
        }
        selectedPoints.clear();
        showingResult = false;
        isError = false;
    }

    /**
     * 处理结果
     */
    private void handleResult() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedPoints.size(); i++) {
            builder.append(selectedPoints.get(i).getNum() + "");
        }
        //密码不正确
        if (!builder.toString().equals(password)) {
            for (int i = 0; i < selectedPoints.size(); i++) {
                Point point = selectedPoints.get(i);
                point.setState(Point.STATE.ERROR);
                isError = true;
            }
        }
        if (onCallBack != null) {
            onCallBack.result(builder.toString(), !isError);
        }
    }

    public interface OnCallBack {
        void result(String password, boolean isRight);
    }
}
