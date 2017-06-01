# NineLockScreen
九宫格解锁
##效果展示
![这里写图片描述](https://github.com/zwb1992/NineLockScreen/blob/master/NineLockScreen/images/ninelockscreen.gif)


##说明
先声明一个Point对象，里面有以下属性

public class Point {
	private float x;//x轴坐标
    private float y;//y轴坐标
    private int radius;//半径
    private int num;//代表的数字--  0 - 8
    private STATE state = STATE.NORMAL;//有三种状态，正常状态，触摸被选中状态，抬起手指密码错误状态

    public enum STATE {
        NORMAL, SELECTED, ERROR
    }
}

在自定义控件NineLockScreenView中先把3*3的9个圆点绘制出来，他们的x轴与y周坐标计算如下

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
	}
	
初始化完成之后，加入到一个list集合当中，这是所有的点，然后监听onTouchEvent事件

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
	
	如果此时正在显示结果，则不响应事件，否则先判断move事件
	
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
	
	就是判断触摸点是否在先前绘制的9个圆点之上，如果在且之前没有被添加入被选中点的集合当中，设置点的状态为选中，且加入集合中，这时候刷新重新绘制
	
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
	
	依次把线连接之前的几个点，最后连接当前的触摸位置，如果没有点被选中，就不绘制
	
	然后手指抬起的时候判断选中点的集合所代表的密码，判断与设置的密码是否一致
	
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
	
	然后延迟一秒钟发送消息，重置当前状态
	
	 private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            reset();
            postInvalidate();
        }
    };
	
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
	
	以上为总体思路！
	