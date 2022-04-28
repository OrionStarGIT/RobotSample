/*
 * Copyright (C) 2017 OrionStar Technology Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ainirobot.robotos.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.maputils.Constant;
import com.ainirobot.robotos.maputils.Pose2d;
import com.ainirobot.robotos.maputils.PoseBean;
import com.ainirobot.robotos.maputils.SpecialPlaceUtil;

import java.util.ArrayList;
import java.util.List;


public class MapView extends View {
    private String TAG = getClass().getSimpleName();
    private static float sArrowSize = 20;

    private Bitmap mBitmap;
    private Matrix matrix;
    private float scaleHistory = 1.0f;
    private float scaleMin = 0.5f;
    private Paint mPaint;
    private PointF mPointDown;
    private boolean mIsStraightLine;
    private EditPath mEditPath;
    private Pose2d mOrigin;
    private Pose2d mCollector;
    private MapMode mMode = MapMode.PREVIEW;
    private List<EditPath> mPathList = new ArrayList<>();
    private int mPathCur = -1;
    private boolean hasForbidLine = false;
//    private Navigator.MODULE_TYPE mEditModuleType;

    private float mEditWidth = 4;
    private int mEditColor = 0xFF000000;
    private OnCollectListener mOnCollectListener;
    private OnPlaceClickListener mOnPlaceClickListener;
    private OnEditHappenListener mOnEditHappenListener;
    private List<Pose2d> mTargets = new ArrayList<>(); // preview when select patrol plan.

    private int width = 0;
    private int height = 0;

    private boolean estimateMove = false;
    private double resolution;
    private float scaleBarRate = 1.0f;
    public static final double BAR_DEFAULT_VALUE = 400; //比例尺默认长度 400cm
    private double SCALE_BAR_STARDAD = 0; //比例尺标准长度
    private double scaleBarHistory = 100; //比例尺长度，历史值
    private double curBarWidth; //比例尺长度，当前
    public static final double BAR_MAX_SCALE_VALUE = 5; //比例尺最大放大尺寸。5cm
    private double laser_width_old = 0;
    private double laser_height_old = 0;
    private OnClickListener mListener;

    public boolean hasPath() {
        return mPathList.size() != 0;
    }

    private int screenWidth;
    private int screenHeight;

    private WindowManager wm;

    public boolean pathCanBack() {
        if (hasPath()) {
            return mPathCur != -1;
        }
        return false;
    }

    public boolean pathCanForward() {
        if (hasPath()) {
            return mPathCur != mPathList.size() - 1;
        }

        return false;
    }

    public void pathBack() {
        if (pathCanBack()) {
            mPathCur--;
            if (mOnEditHappenListener != null) {
                mOnEditHappenListener.onEditHappened();
            }
            invalidate();
        }
    }

    public void pathForward() {
        if (pathCanForward()) {
            mPathCur++;
            if (mOnEditHappenListener != null) {
                mOnEditHappenListener.onEditHappened();
            }
            invalidate();
        }
    }

    public void enterPath() {
        //设置颜色即可
        mPathCur = -1;
        mPathList.clear();
        invalidate();
    }

    public void savePath() {
        Log.d(TAG, "save path");
        if (mPathCur < mPathList.size() - 1) {
            List subList = mPathList.subList(mPathCur + 1, mPathList.size());
            mPathList.removeAll(subList);
        }
        for (EditPath editPath : mPathList) {
            if (editPath.paint.getColor() == Constant.MAPCOLOR.BLOCK) {
                hasForbidLine = true;
            }
        }
        Log.d(TAG, "saveEraser after, mPathList.size = " + mPathList.size());
    }

    public void cancelPath() {
        mPathCur = -1;
        mPathList.clear();
        invalidate();
    }

    public void setStraightLine(boolean isStraightLine) {
        mIsStraightLine = isStraightLine;
    }

    public boolean hasForbidLine() {
        return hasForbidLine;
    }

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean hasEditPath() {
        return mPathList.size() != 0;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (mBitmap != null) {
            initOnce = true;
            scaleMin = Math.min(100f / mBitmap.getWidth(), 100f / mBitmap.getHeight());
            scaleHistory = 1.0f;
            rotateHistory = 0;
            this.scaleBarRate = 1;
        }
        invalidate();
    }

    public void setOrigin(Pose2d origin) {
        Log.d(TAG, "setOrigin");
        mOrigin = origin;
        invalidate();
    }

    public void setResolution(double resolution) {
//        Log.d(TAG,"setResolution: " + resolution);
        this.resolution = resolution;
    }

    public void setTargets(List<Pose2d> targets) {
        mTargets = targets;
        invalidate();
    }

    public void setMode(MapMode mode) {
        mMode = mode;
        mCollector = null;
    }

    public enum MapMode {
        PREVIEW,
        EDIT,// eraser and line
        COLLECT,//estimate mode
        POINT
    }

//    public void setEditModuleType(Navigator.MODULE_TYPE type) {
//        Log.d(TAG, "setEditModuleType:type=" + type);
//        this.mEditModuleType = type;
//    }

    public interface OnCollectListener {
        void onCollect(Pose2d pose2d);
    }

    public interface OnPlaceClickListener {
        void onPlaceClick(String placeName);
    }

    public interface OnEditHappenListener {
        void onEditHappened();
    }

    public void registOnEditHappendListener(OnEditHappenListener listener) {
        mOnEditHappenListener = listener;
    }

    public void registOnPlaceClickListener(OnPlaceClickListener listener) {
        mOnPlaceClickListener = listener;
    }

    public void unRegistOnplaceClickListener() {
        mOnPlaceClickListener = null;
    }

    public void setOnCollectListener(OnCollectListener onCollectListener) {
        mOnCollectListener = onCollectListener;
    }


    public void setEditColor(int color) {
        mEditColor = color;
    }

    public void editBack() {
        if (mPathList.size() > 0) {
            mPathList.remove(mPathList.size() - 1);
            invalidate();
        }
    }

    public void setEditWidth(float width) {
        mEditWidth = width;
    }

    public Bitmap getEditedBitmap() {
        if (mBitmap != null) {
            Canvas canvas = new Canvas(mBitmap);
            for (EditPath editPath : mPathList) {
                canvas.drawPath(editPath.path, editPath.paint);
            }
            mPathList.clear();
            hasForbidLine = false;
        }
        return mBitmap;
    }

    private void init() {
        mPaint = new Paint();
        matrix = new Matrix();
//        matrix.setTranslate(0, 0);

        wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);

    }

    private PointF lastPoint = new PointF();
    private volatile boolean isPinch = false;
    private float lastSpace = 0;
    private float lastRotate = 0;
    private float rotateHistory = 0;

    private PointF invertPoint(Matrix matrix, float x, float y) {
        float[] src = new float[]{x, y};
        float[] dst = new float[2];
        Matrix inv = new Matrix();
        matrix.invert(inv);
        inv.mapPoints(dst, src);
        return new PointF(dst[0], dst[1]);
    }

    private boolean editHappend = false;
    private boolean twoPointer = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent mode: " + mMode + " mIsStraightLine: " + mIsStraightLine);
//        if (mEditModuleType == Navigator.MODULE_TYPE.EDIT_MAP_GUIDE
//                || mEditModuleType == Navigator.MODULE_TYPE.EDIT_MAP_LANGUAGE) {
//            return true;
//        }
        if (mMode == MapMode.COLLECT) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    PointF pointDown = invertPoint(matrix, event.getX(), event.getY());
                    mCollector = new Pose2d(pointDown.x, pointDown.y, 0);
                    estimateMove = false;

                    break;
                case MotionEvent.ACTION_UP:
                    if (mOnCollectListener != null && mCollector != null && estimateMove) {
                        estimateMove = false;
                        mOnCollectListener.onCollect(mCollector);
                    }
                    mCollector = null;
                    invalidate();
                    break;

                case MotionEvent.ACTION_MOVE:
                    PointF pointMove = invertPoint(matrix, event.getX(), event.getY());
                    if (mCollector != null) {
                        estimateMove = true;
                        mCollector.t = 0 - Math.atan2((pointMove.y - mCollector.y),
                                (pointMove.x - mCollector.x));

                        invalidate();
                    }
                    break;
                default:
                    break;
            }
        } else if (mMode == MapMode.EDIT) {//防抖处理
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mEditPath = new EditPath(mEditColor, mEditWidth);
                    mPointDown = invertPoint(matrix, event.getX(), event.getY());
                    mEditPath.path.moveTo(mPointDown.x, mPointDown.y);

                    lastPoint.set(event.getX(), event.getY());
                    editHappend = false;
                    twoPointer = false;
                    break;
                case MotionEvent.ACTION_UP:
                    if (editHappend) {
                        if (mPathCur == mPathList.size() - 1) {
                            //没有过撤销
                            mPathList.add(mEditPath);
                            mPathCur++;
                        } else {
                            //有撤销和前进，采取插入策略
                            mPathList.add(mPathCur + 1, mEditPath);
                            mPathCur++;
                        }
                        if (mOnEditHappenListener != null) {
                            Log.d(TAG, "editHappend");
                            mOnEditHappenListener.onEditHappened();
                        }
                        editHappend = false;
                    }
                    mEditPath = null;
                    twoPointer = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!isPinch && !twoPointer) {
                        PointF pointMove = invertPoint(matrix, event.getX(), event.getY());
                        if (mEditPath != null) {
                            if (curLines() && mIsStraightLine) {
                                mEditPath.path.reset();
                                mEditPath.path.moveTo(mPointDown.x, mPointDown.y);
                            }
                            mEditPath.path.lineTo(pointMove.x, pointMove.y);
                            editHappend = true;
                        }
                    }

                    PointF point = new PointF(event.getX(), event.getY());
                    if (isPinch && mBitmap != null) {
                        float space = pinchSpace(event);
                        float rotate = pinchRotation(event);
                        PointF center = pinchCenter(event);

                        float scale = space / lastSpace;

                        if (scale > 1 && (this.getCurScaleBarValue() == BAR_MAX_SCALE_VALUE)) {
                            //当比例尺为5CM时，不再支持放大
                            break;
                        }

                        if (scale * scaleHistory < scaleMin) {//保证历史累计缩放不能小于1.0f
                            /*scale = 1.0f;
                            scaleHistory = scaleMin;*/
                        } else {
                            scaleHistory = scale * scaleHistory;
                            matrix.postScale(scale, scale, center.x, center.y);
                        }
                        matrix.postRotate(rotate - lastRotate, center.x, center.y);

                        rotateHistory = rotateHistory + (rotate - lastRotate);
                        lastSpace = space;
                        lastRotate = rotate;
                        point = center;

                        float dx = point.x - lastPoint.x;
                        float dy = point.y - lastPoint.y;
                        lastPoint = point;
                        matrix.postTranslate(dx, dy);
                    }


                    invalidate();
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    isPinch = true;
                    twoPointer = true;
                    lastSpace = pinchSpace(event);
                    lastRotate = pinchRotation(event);
                    lastPoint = pinchCenter(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    isPinch = false;
                    break;
                default:
                    break;
            }
        } else if (mMode == MapMode.PREVIEW) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    isPinch = true;
                    lastSpace = pinchSpace(event);
                    lastRotate = pinchRotation(event);
                    lastPoint = pinchCenter(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    isPinch = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    PointF point = new PointF(event.getX(), event.getY());
                    if (isPinch && mBitmap != null) {
                        float space = pinchSpace(event);
                        float rotate = pinchRotation(event);
                        PointF center = pinchCenter(event);

                        float scale = space / lastSpace;

                        Log.d(TAG, "onTouchEvent scale: " + scale);
                        Log.d(TAG, "onTouchEvent getCurScaleBarValue: " + this.getCurScaleBarValue());

                        if (scale > 1 && (this.getCurScaleBarValue() == BAR_MAX_SCALE_VALUE)) {
                            //当比例尺为5CM时，不再支持放大
                            break;
                        }

                        if (scale * scaleHistory < scaleMin) {//保证历史累计缩放不能小于1.0f
                            /*scale = 1.0f;
                            scaleHistory = scaleMin;*/
                        } else {
                            scaleHistory = scale * scaleHistory;
                            matrix.postScale(scale, scale, center.x, center.y);
                        }
                        matrix.postRotate(rotate - lastRotate, center.x, center.y);

                        rotateHistory = rotateHistory + (rotate - lastRotate);
                        lastSpace = space;
                        lastRotate = rotate;
                        point = center;
                    }
                    float dx = point.x - lastPoint.x;
                    float dy = point.y - lastPoint.y;
                    lastPoint = point;
                    matrix.postTranslate(dx, dy);
                    invalidate();
                    break;
                default:
                    break;
            }
        } else if (mMode == MapMode.POINT) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    if (poseBeans != null) {
                        for (int i = 0; i < poseBeans.size(); i++) {
                            PoseBean bean = poseBeans.get(i);
                            Rect rect = getRect(bean.getPose(), false);
                            Matrix tmpMatrix = new Matrix();
                            tmpMatrix.postRotate(-rotateHistory, (float) bean.getPose().x, (float) bean.getPose().y);
                            Matrix myMat = new Matrix();
                            myMat.setConcat(matrix, tmpMatrix);
                            RectF rectf = new RectF(rect);
                            myMat.mapRect(rectf);
                            if (isInRectf(rectf, event)) {
                                String placeName = bean.getName();
                                LogTools.info("placeName"+placeName);

                                if (SpecialPlaceUtil.isNavigatorPoint(Constant.NavigatorPoint.POINT1,
                                        placeName) || SpecialPlaceUtil
                                        .isNavigatorPoint(Constant.NavigatorPoint.POINT2,
                                                placeName)) {
                                    break;
                                }
                                if (RobotApi.getInstance().isChargePileExits() &&
                                        SpecialPlaceUtil.isLocatePole(placeName)) {
                                    break;
                                }
                                bigIconIndex = i;
                                if (mOnPlaceClickListener != null) {
                                    mOnPlaceClickListener.onPlaceClick(bean.getName());
                                }
                                invalidate();
                            }

                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    isPinch = true;
                    lastSpace = pinchSpace(event);
                    lastRotate = pinchRotation(event);
                    lastPoint = pinchCenter(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    isPinch = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    PointF point = new PointF(event.getX(), event.getY());
                    if (isPinch && mBitmap != null) {
                        float space = pinchSpace(event);
                        float rotate = pinchRotation(event);
                        PointF center = pinchCenter(event);

                        float scale = space / lastSpace;

                        if (scale > 1 && (this.getCurScaleBarValue() == BAR_MAX_SCALE_VALUE)) {
                            //当比例尺为5CM时，不再支持放大
                            break;
                        }

                        if (scale * scaleHistory < scaleMin) {//保证历史累计缩放不能小于1.0f
                            /*scale = 1.0f;
                            scaleHistory = scaleMin;*/
                        } else {
                            scaleHistory = scale * scaleHistory;
                            matrix.postScale(scale, scale, center.x, center.y);
                        }
                        matrix.postRotate(rotate - lastRotate, center.x, center.y);

                        rotateHistory = rotateHistory + (rotate - lastRotate);
                        lastSpace = space;
                        lastRotate = rotate;
                        point = center;
                    }
                    float dx = point.x - lastPoint.x;
                    float dy = point.y - lastPoint.y;
                    lastPoint = point;
                    matrix.postTranslate(dx, dy);
                    invalidate();
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    private boolean curLines() {
        return (mEditColor == Constant.MAPCOLOR.BLOCK || mEditColor == Constant.MAPCOLOR.OBSTACLE);
    }

    private boolean isInRectf(RectF rectf, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float left = rectf.left;
        float top = rectf.top;
        float right = rectf.right;
        float bottom = rectf.bottom;
        if (x >= Math.min(left, right) && x <= Math.max(left, right)
                && y >= Math.min(top, bottom) && y <= Math.max(top, bottom)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = measureWidth(widthMeasureSpec);
        height = measureHeight(heightMeasureSpec);
//        left = getPaddingLeft();
//        top = getPaddingTop();
        setMeasuredDimension(width, height);
    }

    boolean initOnce = true;

    private void initMatrix() {
        if (mBitmap != null && initOnce) {
            Log.d(TAG, "width/2 - mBitmap.getWidth()/2 = " + (width / 2 - mBitmap.getWidth() / 2)
                    + ", height/2 - mBitmap.getHeight()/2 = " + (height / 2 - mBitmap.getHeight() / 2));
            initOnce = false;
            matrix.setTranslate(width / 2 - mBitmap.getWidth() / 2, height / 2 - mBitmap.getHeight() / 2);
            float scale = Math.min(1100 * 1f / mBitmap.getWidth(), 1100 * 1f / mBitmap.getHeight());
            matrix.postScale(scale, scale, width / (float) 2, height / (float) 2);
            scaleHistory = scale;

            if (SCALE_BAR_STARDAD == 0) {
                SCALE_BAR_STARDAD = (BAR_DEFAULT_VALUE / (this.resolution * 100));
            }

        }
    }

    private class EditPath {
        Path path;
        Paint paint;

        EditPath(int color, float width) {
            this.path = new Path();
            this.paint = new Paint();
            this.paint.setAntiAlias(false);
//            this.paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.INNER));
            this.paint.setColor(color);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeWidth(width);
            Log.d(TAG, "EditPath width: " + width);
            Log.d(TAG, "EditPath scale: " + scaleHistory);
        }
    }

    private int measureWidth(int measureSpec) {
        int result = getPaddingLeft() + getPaddingRight();
        if (mBitmap != null) {
            result += mBitmap.getWidth();
        }
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = getPaddingTop() + getPaddingBottom();
        if (mBitmap != null) {
            result += mBitmap.getHeight();
        }
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        if (mBitmap == null) {
            return;
        }

        canvas.save();

        initMatrix();
        canvas.concat(matrix);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        if (mPathList.size() != 0) {
            for (int i = 0; i <= mPathCur; i++) {
                canvas.drawPath(mPathList.get(i).path, mPathList.get(i).paint);
            }
        }


        if (mEditPath != null) {
            canvas.drawPath(mEditPath.path, mEditPath.paint);
        }

        canvas.restore();
        drawScale(canvas);    //网格线不进行矩阵转换
        drwaScaleBar(canvas); //网格线比例尺不进行矩阵转换

        canvas.concat(matrix);
        drawCollector(canvas);
        drawTargets(canvas);
        drawPlaceList(poseBeans, canvas);
        drawOrigin(canvas);
    }

    private void drawOrigin(Canvas canvas) {
        if (mOrigin != null) {
            drawArrow(canvas, (float) mOrigin.x, (float) mOrigin.y, 50,
                    (float) mOrigin.t, Color.RED);
        }
    }

    /**
     * 绘制定位手拖的小机器人图标
     *
     * @param canvas
     */
    private void drawCollector(Canvas canvas) {
        if (mCollector != null && mOrigin == null) {
            drawArrow(canvas, (float) mCollector.x, (float) mCollector.y, 100,
                    (float) mCollector.t, Color.GREEN);
        }
    }

    private void drawTargets(Canvas canvas) {
        if (mTargets != null) {
            for (int i = 0; i < mTargets.size(); i++) {
                Pose2d target = mTargets.get(i);
                drawCircleText(canvas, Color.RED, (float) target.x, (float) target.y, "" + i);
            }
        }
    }

    private int bigIconIndex = -1;//一次只有一个bigIconIndex
    private List<PoseBean> poseBeans;

    //加载所有点位信息
    public void setPoseBeans(List<PoseBean> poseBeans) {
        printPlaceBean(poseBeans);
        this.poseBeans = poseBeans;
        invalidate();
    }

    public List<PoseBean> getPoseBeans() {
        return this.poseBeans;
    }

    public boolean haPosBeans() {
        return poseBeans != null && poseBeans.size() > 0;
    }

    public void resetBigIconIndex() {
        bigIconIndex = -1;
        invalidate();
    }

    public void drawPlaceList(List<PoseBean> poseBeans, Canvas canvas) {
        if (poseBeans != null) {
            boolean hasBigIcon = false;
            for (int i = 0; i < poseBeans.size(); i++) {
                if (i == bigIconIndex) {
                    hasBigIcon = true;
                    continue;
                }
                PoseBean poseBean = poseBeans.get(i);
                if (RobotApi.getInstance().isChargePileExits() &&
                        Definition.START_CHARGE_PILE_POSE.equals(poseBean.getName())) {
                    continue;
                }
                Drawable drawable =
                        ContextCompat.getDrawable(getContext(), poseBean.getPose().status == 0 ?
                                R.drawable.normal_bubble_s : R.drawable.map_point_error_icon);
                drawPlace(canvas, poseBean.getPose(), poseBean.getName(), drawable, false);
            }
            if (hasBigIcon) {
                PoseBean poseBean = poseBeans.get(bigIconIndex);
                Drawable drawable =
                        ContextCompat.getDrawable(getContext(), poseBean.getPose().status == 0 ?
                                R.drawable.normal_bubble_s : R.drawable.map_point_error_icon);
                drawPlace(canvas, poseBeans.get(bigIconIndex).getPose(), poseBeans.get(bigIconIndex).getName(), drawable, true);
            }
        }
    }


    private void drawPlace(Canvas canvas, Pose2d pose2d, String name, Drawable icon, boolean bigIcon) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-rotateHistory, (float) pose2d.x, (float) pose2d.y);
        canvas.save();
        canvas.concat(matrix);
        Rect rectIcon = getRect(pose2d, bigIcon);
        //绘制icon
        icon.setBounds(rectIcon);
        icon.draw(canvas);

        //绘制text
        int textSize = (int) Math.ceil(Math.max(32 / scaleHistory, 3));
//        Log.d(TAG,"drawPlace name: " + name);
//        Log.d(TAG,"drawPlace textSize: " + textSize);
        int dTop = 8;
        Paint mPaint = new Paint();
        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name, (float) pose2d.x, (rectIcon.top - Math.max(dTop / scaleHistory, 1)), mPaint);
        canvas.restore();
    }

    private Rect getRect(Pose2d pose2d, boolean bigIcon) {
        float x = (float) pose2d.x;
        float y = (float) pose2d.y;
        float width = bigIcon ? 160 : 80;
        float heigth = bigIcon ? 204 : 102;
        int left = (int) (x - (width / 2) / scaleHistory);
        int right = (int) (x + (width / 2) / scaleHistory);
        int scaleWidth = right - left;
        if (scaleWidth < 4) {
            left = left - (4 - scaleWidth) / 2;
            right = right + (4 - scaleWidth) / 2;
            scaleWidth = right - left;
        }
        int top = (int) (y - heigth * scaleWidth / width);
        return new Rect(left, top, right, (int) y);
    }

    private void drawCircleText(Canvas canvas, int color, float x, float y, String text) {
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, 10, paint);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(12);
        canvas.drawText(text, x - 4 * text.length(), y + 4, paint);
    }


    private void drwaScaleBar(Canvas canvas) {

        canvas.save();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth((float) 3.0);
        paint.setTextSize(30);
        //paint.setAlpha(100);

        this.curBarWidth = SCALE_BAR_STARDAD * scaleBarRate * this.scaleHistory;

        if (curBarWidth > this.getEnlargeBarRate() * this.scaleBarHistory) {
            scaleBarRate /= this.getEnlargeBarRate();
            curBarWidth = SCALE_BAR_STARDAD * scaleBarRate * this.scaleHistory;
            this.scaleBarHistory = curBarWidth;
        }

        if (curBarWidth < this.scaleBarHistory / this.getNarrowBarRate()) {
            scaleBarRate *= this.getNarrowBarRate();
            curBarWidth = SCALE_BAR_STARDAD * scaleBarRate * this.scaleHistory;
            this.scaleBarHistory = curBarWidth;
        }

//        Log.d(TAG,"drwaScaleBar barWidth: " + curBarWidth);
//        Log.d(TAG,"drwaScaleBar scaleHistory: " + scaleHistory);
//        Log.d(TAG,"drwaScaleBar resolution: " + this.resolution);
//        Log.d(TAG,"drwaScaleBar scaleBarHistory: " + this.scaleBarHistory);
//        Log.d(TAG,"drwaScaleBar scaleBarRate: " + this.scaleBarRate);
//        Log.d(TAG,"drwaScaleBar getEnlargeBarRate: " + this.getEnlargeBarRate());


        float bar_start_x = 100;
        float bar_start_y = 250;

        float bar_stop_x = bar_start_x + (float) curBarWidth;

        float content_start_x = 100;
        float content_start_y = 240;

        String barContent = this.getScaleBarContent();

        Rect bounds = new Rect();
        paint.getTextBounds(barContent, 0, barContent.length(), bounds);
        float textWidth = bounds.right - bounds.left;
        content_start_x = content_start_x + (float) (this.curBarWidth / 2) - textWidth / 2;

        canvas.drawText(this.getScaleBarContent(), content_start_x, content_start_y, paint);
        canvas.drawLine(bar_start_x, (float) bar_start_y, bar_stop_x, (float) bar_start_y, paint);
        canvas.drawLine(bar_start_x, (float) bar_start_y, bar_start_x, (float) (bar_start_y - 5), paint);
        canvas.drawLine(bar_stop_x, (float) bar_start_y, bar_stop_x, (float) (bar_start_y - 5), paint);

        canvas.restore();

    }


    /**
     * 返回当前的比例尺
     *
     * @return
     */
    private int getCurScaleBarValue() {

        long result;

        double value = (double) (SCALE_BAR_STARDAD * scaleBarRate * this.resolution * 100);

        result = Math.round(value);

        return (int) result;
    }

    /**
     * 获取缩小的比例尺调整倍率
     *
     * @return
     */
    private double getNarrowBarRate() {

        int curBar = this.getCurScaleBarValue();
        double result = 2;

        switch (curBar) {
            case 1:
                result = 2;
                break;
            case 2:
                result = 2.5;
                break;
            case 5:
                result = 2;
                break;
            case 10:
                result = 2;
                break;
            case 20:
                result = 2.5;
                break;
            case 50:
                result = 2;
                break;
            case 100:
                result = 2;
                break;
            case 200:
                result = 2.5;
                break;
            case 500:
                result = 2;
                break;
            case 1000:
                result = 2;
                break;
            case 2000:
                result = 2.5;
                break;
            case 5000:
                result = 2;
                break;
            case 10000:
                result = 2;
                break;
            case 20000:
                result = 2.5;
                break;
        }
        return result;
    }

    /**
     * 获取放大的比例尺调整倍率
     *
     * @return
     */
    private double getEnlargeBarRate() {

        int curBar = this.getCurScaleBarValue();
        double result = 2;

        switch (curBar) {

            case 20000:
                result = 2;
                break;
            case 10000:
                result = 2;
                break;
            case 5000:
                result = 2.5;
                break;
            case 2000:
                result = 2;
                break;
            case 1000:
                result = 2;
                break;
            case 500:
                result = 2.5;
                break;
            case 200:
                result = 2;
                break;
            case 100:
                result = 2;
                break;
            case 50:
                result = 2.5;
                break;
            case 20:
                result = 2;
                break;
            case 10:
                result = 2;
                break;
            case 5:
                result = 2.5;
                break;
            case 2:
                result = 2;
                break;
        }
        return result;
    }


    /**
     * 获取比例尺单位
     *
     * @return
     */
    private String getScaleBarContent() {
        String result;

        double value = (double) (SCALE_BAR_STARDAD * scaleBarRate * this.resolution * 100);

//        Log.d(TAG,"getScaleBarContent: " + value);

        if (value < 100) {
            result = Math.round(value) + "CM";
        } else {
            result = Math.round(value / 100) + "M";
        }

        return result;
    }


    /**
     * 绘制比例尺网格线
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas) {

        this.curBarWidth = SCALE_BAR_STARDAD * scaleBarRate * this.scaleHistory;

        double lineSpace = this.curBarWidth;

//        Log.d(TAG, "drawScale lineSpace: " + lineSpace);
//        Log.d(TAG, "drawScale screenWidth: " + screenWidth);
//        Log.d(TAG, "drawScale screenHeight: " + screenHeight);

        canvas.save();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth((float) 1.0);
        paint.setAlpha(100);

        //画横线,上半部分
        double rowLine_startY_upper = (screenHeight / 2) - lineSpace;

        for (int i = 0; i < 1000; i++) {
            canvas.drawLine(0, (float) rowLine_startY_upper, 1480, (float) rowLine_startY_upper, paint);
            rowLine_startY_upper -= lineSpace;
            if (rowLine_startY_upper < 0) break;
        }


        //画横线,下半部分
        int rowLine_startY_bottom = screenHeight / 2;
        for (int i = 0; i < 1000; i++) {
            canvas.drawLine(0, (float) rowLine_startY_bottom, 1480, (float) rowLine_startY_bottom, paint);
            rowLine_startY_bottom += lineSpace;
            if (rowLine_startY_bottom > screenHeight) break;
        }


        //画竖线,左半部分
        double columnLine_startX_left = screenWidth / 2 - lineSpace;
        for (int i = 0; i < 1000; i++) {
            canvas.drawLine((float) columnLine_startX_left, 0, (float) columnLine_startX_left, 1920, paint);
            columnLine_startX_left -= lineSpace;
            if (columnLine_startX_left < 0) break;
        }

        //画竖线,右半部分
        double columnLine_startX_right = screenWidth / 2;
        for (int i = 0; i < 1000; i++) {
            canvas.drawLine((float) columnLine_startX_right, 0, (float) columnLine_startX_right, 1920, paint);
            columnLine_startX_right += lineSpace;
            if (columnLine_startX_right > screenWidth) break;
        }

        canvas.restore();
    }

    ;

    /**
     * 绘制机器人图标
     *
     * @param canvas
     * @param x
     * @param y
     * @param len
     * @param angle
     * @param color
     */
    private void drawArrow(Canvas canvas, float x, float y, float len, float angle, int color) {
        canvas.save();
        angle = 0 - angle;
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(8);

        Matrix tempMat = new Matrix();
        tempMat.postRotate((float) (Math.toDegrees(angle) + 90), x, y);
        canvas.concat(tempMat);

        //绘制底盘icon
        double bwidth = 200 * scaleHistory * this.resolution;
        double bheight = 200 * scaleHistory * this.resolution;

        float b_ex = x;
        float b_ey = (float) (y - bheight / scaleHistory);
        Drawable robotBottomIcon = ContextCompat.getDrawable(getContext(), R.drawable.robot_bottom);
        robotBottomIcon.setBounds(new Rect((int) (x - (bwidth / (float) 2) / scaleHistory), (int) (y - (bheight / (float) 2) / scaleHistory),
                (int) (x + (bwidth / (float) 2) / scaleHistory), (int) (y + (bheight / (float) 2) / scaleHistory)));
        robotBottomIcon.draw(canvas);

        //绘制激光扫描区icon
        double laser_width = 1200 * scaleHistory * this.resolution;
        double laser_height = 1200 * scaleHistory * this.resolution;

        if (laser_width < laser_width_old) {
            if (bwidth <= 60) {
                laser_width = 362;
                laser_height = 362;
            }
        }

        laser_width_old = laser_width;
        laser_height_old = laser_height;

//        Log.d(TAG,"bWidth: " + bwidth);
//        Log.d(TAG,"laser_width: " + laser_width);
//        Log.d(TAG,"robot width: " + 150);
        /*float ex = (float) (x + Math.cos(angle) * heigth);
        float ey = (float) (y + Math.sin(angle) * heigth);*/

        float laser_deltaY = y + 120 / scaleHistory;
        float laser_ex = x;
        float laser_ey = (float) (laser_deltaY - laser_height / scaleHistory);
        Drawable robotLaserIcon = ContextCompat.getDrawable(getContext(), R.drawable.robot_laser);
        robotLaserIcon.setBounds(new Rect((int) (x - (laser_width / (float) 2) / scaleHistory), (int) (y - (laser_height / (float) 2) / scaleHistory),
                (int) (x + (laser_width / (float) 2) / scaleHistory), (int) (y + (laser_height / (float) 2) / scaleHistory)));
        robotLaserIcon.draw(canvas);

        //绘制机器人icon
        int width = 150;
        int height = 150;
        /*float ex = (float) (x + Math.cos(angle) * heigth);
        float ey = (float) (y + Math.sin(angle) * heigth);*/
        float ex = x;
        float ey = (float) (y - height / scaleHistory);

        int robot_deltaX = Math.round((width / (float) 2) / scaleHistory);
        int robot_deltaY = Math.round(((height / (float) 2) / scaleHistory));

        if (robot_deltaX <= 1) {
            robot_deltaX = 1;
        }
        if (robot_deltaY <= 1) {
            robot_deltaY = 1;
        }

//        Log.d(TAG,"robot_deltaX: " + robot_deltaX);
//        Log.d(TAG,"robot_deltaY: " + robot_deltaY);
//        Log.d(TAG,"robot_width: " + robot_deltaX * scaleHistory);
//        Log.d(TAG,"robot_height: " + robot_deltaY * scaleHistory);
//        Log.d(TAG,"robot position x: " + x);
//        Log.d(TAG,"robot position y: " + y);

        Drawable robotIcon = ContextCompat.getDrawable(getContext(), R.drawable.robot);
        robotIcon.setBounds(new Rect((int) (x - robot_deltaX), (int) (y - robot_deltaY),
                (int) (x + robot_deltaX), (int) (y + robot_deltaY)));
        robotIcon.draw(canvas);

        canvas.restore();
//        canvas.drawLine(x, y, ex, ey, paint);

        /*double arrowHeight = sArrowSize;
        double arrowHalfWidth = sArrowSize / 2.2;

        double tmpAngle = Math.atan(arrowHalfWidth / len);
        double tmpLen = arrowHalfWidth / Math.sin(tmpAngle);

        float lx = Double.valueOf(Math.cos(angle + tmpAngle) * tmpLen).floatValue() + x;
        float ly = Double.valueOf(Math.sin(angle + tmpAngle) * tmpLen).floatValue() + y;

        float rx = Double.valueOf(Math.cos(angle - tmpAngle) * tmpLen).floatValue() + x;
        float ry = Double.valueOf(Math.sin(angle - tmpAngle) * tmpLen).floatValue() + y;

        float tx = Double.valueOf(x + Math.cos(angle) * (len + arrowHeight)).floatValue();
        float ty = Double.valueOf(y + Math.sin(angle) * (len + arrowHeight)).floatValue();
        Path path = new Path();
        path.moveTo(lx, ly);
        path.lineTo(tx, ty);
        path.lineTo(rx, ry);
        path.close();
        canvas.drawPath(path, paint);*/
    }


    private float pinchSpace(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    private PointF pinchCenter(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        try {
            x = event.getX(0) + event.getX(1);
            y = event.getY(0) + event.getY(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PointF(x / 2, y / 2);
    }

    private float pinchRotation(MotionEvent event) {
        double delta_x = event.getX(0);
        double delta_y = event.getY(0);
        try {
            delta_x = (event.getX(0) - event.getX(1));
            delta_y = (event.getY(0) - event.getY(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private void printPlaceBean(List<PoseBean> poseBeans) {
        Log.d(TAG, "printPlaceBean: size=" + (poseBeans == null ? 0 : poseBeans.size()));
        for (PoseBean bean : poseBeans) {
            Log.d(TAG, "printPlaceBean: placeList=" + bean.toString());
        }
    }
}
