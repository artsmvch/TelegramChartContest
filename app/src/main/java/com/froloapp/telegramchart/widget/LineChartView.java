//package com.froloapp.telegramchart.widget;
//
//import android.animation.ValueAnimator;
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.util.Property;
//import android.view.MotionEvent;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.view.animation.Interpolator;
//
//import com.froloapp.telegramchart.R;
//
//
//public class LineChartView extends AbsChartView {
//    // static
//    private static final int DEFAULT_TEXT_HEIGHT_IN_SP = 15;
//    private static final int TOUCH_STAMP_THRESHOLD_IN_DP = 5;
//    private static final long ANIM_DURATION = 200L;
//
//    // paint tools
//    private final Paint stampInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private float stampInfoBigDotRadius;
//    private float stampInfoSmallDotRadius;
//    private int stampInfoSmallDotColor;
//
//    // touch
//    private boolean wasClickedStamp = false;
//    private long clickedStamp;
//    private float clickedXPosition;
//    private float clickedStampAlpha;
//    private OnStampClickListener onStampClickListener;
//    private ValueAnimator clickedStampAnimator;
//    private final Interpolator clickedStampInterpolator = new AccelerateDecelerateInterpolator();
//
//    private final static Property<LineChartView, Float> CLICKED_STAMP_ALPHA = new Property<LineChartView, Float>(float.class, "clickedStampAlpha") {
//        @Override public Float get(LineChartView object) {
//            return object.clickedStampAlpha;
//        }
//        @Override public void set(LineChartView object, Float value) {
//            object.clickedStampAlpha = value;
//            object.invalidate();
//        }
//    };
//
//    public LineChartView(Context context) {
//        this(context, null);
//    }
//
//    public LineChartView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context, attrs);
//    }
//
//    private void init(Context context, AttributeSet attrs) {
//        if (attrs != null) {
//            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LineChartView, 0, 0);
//            stampInfoSmallDotColor = typedArray.getColor(R.styleable.LineChartView_clickedStampSmallDotColor, getXAxisColor());
//            typedArray.recycle();
//        } else {
//            stampInfoSmallDotColor = getXAxisColor();
//        }
//
//        // stamp info paint
//        stampInfoPaint.setStrokeWidth(Utils.dpToPx(1f, context));
//        stampInfoPaint.setStyle(Paint.Style.STROKE);
//
//        stampInfoBigDotRadius = Utils.dpToPx(4f, context);
//        stampInfoSmallDotRadius = Utils.dpToPx(2f, context);
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//
//        ValueAnimator a = clickedStampAnimator;
//        if (a != null) a.cancel();
//    }
//
//    public interface OnStampClickListener {
//        void onTouchDown(LineChartView view, long timestamp, float timestampX);
//        void onTouchUp(LineChartView view);
//    }
//
//    public void setOnStampClickListener(OnStampClickListener l) {
//        this.onStampClickListener = l;
//    }
//
//    /* *********************************
//     ********** HELPER METHODS *********
//     ***********************************/
//
//    @Override
//    final boolean drawFooter() {
//        return true;
//    }
//
//    /* *******************************
//     ******** DRAWING METHODS ********
//     ****************************** */
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        drawXAxis(canvas);
//        drawClickedTimestampBar(canvas);
//        drawYAxis(canvas);
//        drawLines(canvas);
//        drawClickedTimestampDots(canvas);
//    }
//
//    private void drawClickedTimestampBar(Canvas canvas) {
//        LineChartAdapter adapter = getAdapter();
//        if (adapter != null && wasClickedStamp) {
//            float xPosition = clickedXPosition;
//            float x = getXCoor(xPosition);
//            stampInfoPaint.setAlpha(255);
//            stampInfoPaint.setStyle(Paint.Style.STROKE);
//            stampInfoPaint.setColor(getXAxisColor());
//            canvas.drawLine(x, getPaddingTop(), x, getMeasuredHeight() - getPaddingBottom() - getFooterHeight(), stampInfoPaint);
//        }
//    }
//
//    private void drawClickedTimestampDots(Canvas canvas) {
//        LineChartAdapter adapter = getAdapter();
//        if (adapter != null && wasClickedStamp) {
//            long xAxis = clickedStamp;
//            float xPosition = clickedXPosition;
//            float x = getXCoor(xPosition);
//
//            OldLine fadedChart = getFadedChart();
//            for (int i = 0; i < adapter.getLineCount(); i++) {
//                OldLine chart = adapter.getLineAt(i);
//                boolean needToDraw;
//                float alpha;
//                if (chart == fadedChart) {
//                    needToDraw = true;
//                    alpha = getFadedChartAlpha();
//                } else if (adapter.isLineEnabled(chart)) {
//                    needToDraw = true;
//                    alpha = 1f;
//                } else {
//                    needToDraw = false;
//                    alpha = 0f;
//                }
//
//                if (needToDraw) {
//                    // drawing dots
//                    stampInfoPaint.setStyle(Paint.Style.FILL);
//                    stampInfoPaint.setColor(chart.getColor());
//                    stampInfoPaint.setAlpha((int) (alpha * 255));
//                    int index = adapter.getTimestampIndex(xAxis);
//                    float value = chart.getValueAt(index);
//                    float y = getYCoor(value);
//                    canvas.drawCircle(x, y, stampInfoBigDotRadius, stampInfoPaint);
//                    stampInfoPaint.setColor(stampInfoSmallDotColor);
//                    stampInfoPaint.setAlpha((int) (alpha * 255));
//                    canvas.drawCircle(x, y, stampInfoSmallDotRadius, stampInfoPaint);
//                }
//            }
//        }
//    }
//
//    /* *********************************
//     ********* TOUCH CALLBACKS *********
//     **********************************/
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//        //wasClickedStamp = false;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                getParent().requestDisallowInterceptTouchEvent(true);
//                float x = event.getX();
//                handleTouch(x);
//                return true;
//            }
//            case MotionEvent.ACTION_MOVE: {
//                float x = event.getX();
//                handleTouch(x);
//                break;
//            }
//            case MotionEvent.ACTION_UP: {
//                wasClickedStamp = false;
//                getParent().requestDisallowInterceptTouchEvent(false);
//                dispatchTouchUp();
//                invalidate();
//                break;
//            }
//        }
//        return super.onTouchEvent(event);
//    }
//
//    private void handleTouch(float x) {
//        LineChartAdapter adapter = getAdapter();
//        if (adapter != null) {
//            float xPosition = getXPosition(x);
//            //this.clickedXPosition = adapter.getClosestTimestampPosition(xPosition);
//            // looking for the closest X axis stamp
//            long closestTimestamp = adapter.getClosestTimestamp(xPosition);
//            if (clickedStamp != closestTimestamp) { // has changed
//                this.clickedStamp = closestTimestamp;
//                this.clickedXPosition = adapter.getTimestampRelPosition(closestTimestamp);
//
//                float left = getXCoor(clickedXPosition) + 10; // x coor of clicked timestamp + margin(10 by default)
//                dispatchTouchDown(closestTimestamp, left);
//
//                // this view should know that a stamp was clicked
//                this.wasClickedStamp = true;
//                invalidate();
//            }
//        }
//    }
//
//    private void dispatchTouchDown(long timestamp, float timestampX) {
//        OnStampClickListener l = this.onStampClickListener;
//        if (l != null) {
//            l.onTouchDown(this, timestamp, timestampX);
//        }
//    }
//
//    private void dispatchTouchUp() {
//        OnStampClickListener l = this.onStampClickListener;
//        if (l != null) {
//            l.onTouchUp(this);
//        }
//    }
//
//    private void fadeInClickedStamp() {
//        ValueAnimator a = clickedStampAnimator;
//        if (a != null) a.cancel();
//    }
//
//    @Override
//    public void setAdapter(LineChartAdapter adapter, boolean animate) {
//        wasClickedStamp = false;
//        super.setAdapter(adapter, animate);
//    }
//
//    public void clearClickedStamp() {
//        // apply fade in to clicked timestamp here
//        wasClickedStamp = false;
//        invalidate();
//    }
//}