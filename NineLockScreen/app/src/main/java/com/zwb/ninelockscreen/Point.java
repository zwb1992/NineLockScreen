package com.zwb.ninelockscreen;

/**
 * Created by zwb
 * Description 每个圆点
 * Date 2017/6/1.
 */

public class Point {
    private float x;//x轴坐标
    private float y;//y轴坐标
    private int radius;//半径
    private int num;//代表的数字--  0 - 8
    private STATE state = STATE.NORMAL;//有三种状态，正常状态，触摸被选中状态，抬起手指密码错误状态

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public enum STATE {
        NORMAL, SELECTED, ERROR
    }

    public Point() {
    }

    public Point(float x, float y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Point(float x, float y, int radius, int num) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.num = num;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", radius=" + radius +
                ", num=" + num +
                '}';
    }

    public boolean contains(float x, float y) {
        float fx = this.x - x;
        float fy = this.y - y;
        return Math.sqrt(fx * fx + fy * fy) <= radius;
    }
}
