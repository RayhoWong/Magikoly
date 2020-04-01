package com.xfeng.beautyfacelib.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xfengimacgomo
 * data 2019-06-13 11:57
 * email xfengv@yeah.net
 */
public class EyesInfo {
    private Position leftEyesPosition;
    private Position rightEyesPosition;
    ;
    private List<Position> leftEyesPoint; //一般是16个点
    private List<Position> rightEyesPoint;

    private float[] leftFace;
    private float[] rightFace;
    private float[] deltaArray;

    public float[] getLeftFace() {
        return leftFace != null ? leftFace : new float[0];
    }

    public void setLeftFace(float[] leftFace) {
        this.leftFace = leftFace;
    }

    public float[] getRightFace() {
        return rightFace != null ? rightFace : new float[0];
    }

    public void setRightFace(float[] rightFace) {
        this.rightFace = rightFace;
    }

    public float[] getDeltaArray() {
        return deltaArray != null ? deltaArray : new float[0];
    }

    public void setDeltaArray(float[] deltaArray) {
        this.deltaArray = deltaArray;
    }

    public Position getLeftEyesPosition() {
        return leftEyesPosition != null ? leftEyesPosition : new Position(0f, 0f);
    }

    public void setLeftEyesPosition(Position leftEyesPosition) {
        this.leftEyesPosition = leftEyesPosition;
    }

    public Position getRightEyesPosition() {
        return rightEyesPosition != null ? rightEyesPosition : new Position(0f, 0f);
    }

    public void setRightEyesPosition(Position rightEyesPosition) {
        this.rightEyesPosition = rightEyesPosition;
    }

    public List<Position> getLeftEyesPoint() {
        return leftEyesPoint != null ? leftEyesPoint : new ArrayList<Position>();
    }

    public void setLeftEyesPoint(List<Position> leftEyesPoint) {
        this.leftEyesPoint = leftEyesPoint;
    }

    public List<Position> getRightEyesPoint() {
        return rightEyesPoint != null ? rightEyesPoint : new ArrayList<Position>();
    }

    public void setRightEyesPoint(List<Position> rightEyesPoint) {
        this.rightEyesPoint = rightEyesPoint;
    }

    public static class Position {
        float x;
        float y;

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
