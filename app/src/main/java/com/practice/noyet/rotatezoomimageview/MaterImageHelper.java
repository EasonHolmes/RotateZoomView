package com.practice.noyet.rotatezoomimageview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.math.BigDecimal;

/**
 * Created by cuiyang on 16/2/18.
 */
public class MaterImageHelper implements View.OnTouchListener {

    private ImageView imageView;
    private Activity mContext;

    private PointF point0 = new PointF();
    private PointF pointM = new PointF();

    private final int NONE = 0;
    /**
     * 平移
     */
    private final int DRAG = 1;
    /**
     * 旋转、缩放
     */
    private final int ZOOM = 2;
    /**
     * 设定事件模式
     */
    private int mode = NONE;
    /**
     * 图片缩放矩阵
     */
    private Matrix matrix = new Matrix();
    /**
     * 保存触摸前的图片缩放矩阵
     */
    private Matrix savedMatrix = new Matrix();
    /**
     * 保存触点移动过程中的图片缩放矩阵
     */
    private Matrix matrix1 = new Matrix();
    /**
     * 屏幕高度
     */
    private int displayHeight;
    /**
     * 屏幕宽度
     */
    private int displayWidth;
    /**
     * 最小缩放比例
     */
    protected float minScale = 1f;
    /**
     * 最大缩放比例
     */
    protected float maxScale = 3f;
    /**
     * 当前缩放比例
     */
    protected float currentScale = 1f;
    /**
     * 多点触摸2个触摸点间的起始距离
     */
    private float oldDist;
    /**
     * 多点触摸时图片的起始角度
     */
    private float oldRotation = 0;
    /**
     * 旋转角度
     */
    protected float rotation = 0;
    /**
     * 图片初始宽度
     */
    private int imgWidth;
    /**
     * 图片初始高度
     */
    private int imgHeight;
    /**
     * 设置单点触摸退出Activity时，单点触摸的灵敏度（可针对不同手机单独设置）
     */
    protected final int MOVE_MAX = 2;
    /**
     * 单点触摸时手指触发的‘MotionEvent.ACTION_MOVE’次数
     */
    private int fingerNumMove = 0;

    private Bitmap bm;
    /**
     * 保存matrix缩放比例
     */
    private float matrixScale = 1;

    public MaterImageHelper(Activity mContext, ImageView imageView) {
        this.imageView = imageView;
        this.mContext = mContext;
        initData();
    }


    public void initData() {
        imageView.setOnTouchListener(this);
        bm = createBitmapFromView(imageView);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;
        showImage();
    }

    private Bitmap createBitmapFromView(ImageView v) {
        v.setDrawingCacheEnabled(true);
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0,
                v.getMeasuredWidth(), v.getMeasuredHeight());
        v.buildDrawingCache(true);
        Bitmap bmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bmap;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ImageView imageView = (ImageView) view;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                point0.set(event.getX(), event.getY());
                mode = DRAG;
                Log.e("ccc", "MotionEvent--ACTION_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                oldRotation = rotation(event);
                savedMatrix.set(matrix);
                setMidPoint(pointM, event);
                mode = ZOOM;
                Log.e("ccc", "MotionEvent--ACTION_POINTER_DOWN---" + oldRotation);
                break;
            case MotionEvent.ACTION_UP:
//                if (mode == DRAG && (fingerNumMove <= MOVE_MAX)) {
//                    mContext.finish();
//                }
                checkView();
                centerAndRotate();
                imageView.setImageMatrix(matrix);
                Log.e("ccc", "MotionEvent--ACTION_UP");
                fingerNumMove = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.e("ccc", "MotionEvent--ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                operateMove(event);
                imageView.setImageMatrix(matrix1);
                fingerNumMove++;
                Log.e("ccc", "MotionEvent--ACTION_MOVE");
                break;

        }
        return true;
    }

    public void recyclerImg() {
        if (bm != null && !bm.isRecycled()) {
            bm.recycle(); // 回收图片所占的内存
            bm = null;
            System.gc(); // 提醒系统及时回收
        }
    }

    /**
     * 显示图片
     */
    private void showImage() {
        imgWidth = bm.getWidth();
        imgHeight = bm.getHeight();
        imageView.setImageBitmap(bm);
        matrix.setScale(1, 1);
        centerAndRotate();
        imageView.setImageMatrix(matrix);
    }

    /**
     * 触点移动是的操作
     *
     * @param event 触摸事件
     */
    private void operateMove(MotionEvent event) {
        matrix1.set(savedMatrix);
        switch (mode) {
            case DRAG:
                matrix1.postTranslate(event.getX() - point0.x, event.getY() - point0.y);
                break;
            case ZOOM:
                rotation = rotation(event) - oldRotation;
                float newDist = spacing(event);
                float scale = newDist / oldDist;
                currentScale = (scale > 3.5f) ? 3.5f : scale;
                Log.e("ccc", "缩放倍数---" + currentScale);
                Log.e("ccc", "旋转角度---" + rotation);
                matrix1.postScale(currentScale, currentScale, pointM.x, pointM.y);// 縮放
                matrix1.postRotate(rotation, displayWidth / 2, displayHeight / 2);// 旋轉
                break;
        }
    }

    /**
     * 两个触点的距离
     *
     * @param event 触摸事件
     * @return float
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 取旋转角度
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 两个触点的中间坐标
     *
     * @param pointM 中间坐标
     * @param event  触摸事件
     */
    private void setMidPoint(PointF pointM, MotionEvent event) {
        float x = event.getX(0) + event.getY(1);
        float y = event.getY(0) + event.getY(1);
        pointM.set(x / 2, y / 2);
    }

    /**
     * 检查约束条件(缩放倍数)
     */
    private void checkView() {
        if (currentScale > 1) {
            if (currentScale * matrixScale > maxScale) {
                matrix.postScale(maxScale / matrixScale, maxScale / matrixScale, pointM.x, pointM.y);
                matrixScale = maxScale;
            } else {
                matrix.postScale(currentScale, currentScale, pointM.x, pointM.y);
                matrixScale *= currentScale;
            }
        } else {
            if (currentScale * matrixScale < minScale) {
                matrix.postScale(minScale / matrixScale, minScale / matrixScale, pointM.x, pointM.y);
                matrixScale = minScale;
            } else {
                matrix.postScale(currentScale, currentScale, pointM.x, pointM.y);
                matrixScale *= currentScale;
            }
        }
    }

    /**
     * 图片居中显示、判断旋转角度 小于（90 * x + 45）度图片旋转（90 * x）度 大于则旋转（90 * (x+1)）
     */
    private void centerAndRotate() {
        RectF rect = new RectF(0, 0, imgWidth, imgHeight);
        matrix.mapRect(rect);
        float width = rect.width();
        float height = rect.height();
        float dx = 0;
        float dy = 0;

        if (width < displayWidth) {
            dx = displayWidth / 2 - width / 2 - rect.left;
        } else if (rect.left > 0) {
            dx = -rect.left;
        } else if (rect.right < displayWidth) {
            dx = displayWidth - rect.right;
        }

        if (height < displayHeight) {
            dy = displayHeight / 2 - height / 2 - rect.top;
        } else if (rect.top > 0) {
            dy = -rect.top;
        } else if (rect.bottom < displayHeight) {
            dy = displayHeight - rect.bottom;
        }

        matrix.postTranslate(dx, dy);

        /** 图片被放大后无法进行缩放 */
        if (rotation != 0) {
            int rotationNum = (int) (rotation / 90);
            float rotationAvai = new BigDecimal(rotation % 90).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            float realRotation = 0;
            if (rotation > 0) {
                realRotation = rotationAvai > 45 ? (rotationNum + 1) * 90 : rotationNum * 90;
            } else if (rotation < 0) {
                realRotation = rotationAvai < -45 ? (rotationNum - 1) * 90 : rotationNum * 90;
            }
            Log.e("ccc", "realRotation: " + realRotation);
            matrix.postRotate(realRotation, displayWidth / 2, displayHeight / 2);
            rotation = 0;
        }
    }
}
