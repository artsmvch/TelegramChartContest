package com.froloapp.telegramchart.widget.linechartview.factory;


import android.util.SparseIntArray;

import com.froloapp.telegramchart.widget.Utils;
import com.froloapp.telegramchart.widget.linechartview.Line;
import com.froloapp.telegramchart.widget.linechartview.LineChartAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


class SimpleLineChartAdapter implements LineChartAdapter {
    // static
    private static final int DEFAULT_MIN_VALUE = -10;
    private static final int DEFAULT_MAX_VALUE = 10;

    private static AtomicInteger chartId = new AtomicInteger(0);

    private static int nextChartId() {
        return chartId.getAndAdd(1);
    }

    private final int id;

    private final List<Long> timestamps = new ArrayList<>();
    private final List<String> timestampTexts = new ArrayList<>();

    private List<LineHolder> lineHolders = new ArrayList<>();
    private final SparseIntArray localMinimums = new SparseIntArray(); // local minimums at indexes
    private final SparseIntArray localMaximums = new SparseIntArray(); // local maximums at indexes

    private static class LineHolder {
        final Line data;
        boolean visible;
        LineHolder(Line data, boolean visible) {
            this.data = data;
            this.visible = visible;
        }
    }

    SimpleLineChartAdapter(List<Long> timestamps, List<Line> lines) {
        id = nextChartId();

        lineHolders.clear();
        this.timestamps.addAll(timestamps);
        if (!timestamps.isEmpty()) {
            Collections.sort(timestamps); // default sort
            for (Line data : lines) {
                lineHolders.add(new LineHolder(data, true));
            }
        }

        // save local minimums nad maximums
        calcMinimumsAndMaximums();
        obtainTimestampTexts();
    }

    private void obtainTimestampTexts() {
        timestampTexts.clear();
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String text = Utils.getMonthString(month) + ' ' + day;
            timestampTexts.add(text);
        }
    }

    private void calcMinimumsAndMaximums() {
        localMinimums.clear();
        localMaximums.clear();
        for (int i = 0; i < timestamps.size(); i++) {
            int minValue = findMinValueAt(i);
            int maxValue = findMaxValueAt(i);
            localMinimums.put(i, minValue);
            localMaximums.put(i, maxValue);
        }
    }

    @Override
    public int getTimestampCount() {
        return timestamps.size();
    }

    @Override
    public long getTimestampAt(int index) {
        return timestamps.get(index);
    }

    @Override
    public int getLeftClosestTimestampIndex(float toXPosition) {
        if (timestamps.isEmpty()) return -1; // early return

        long minTimestamp = timestamps.get(0);
        long maxTimestamp = timestamps.get(timestamps.size() - 1);
        long approximatelyTimestamp = minTimestamp + (long) ((maxTimestamp - minTimestamp) * toXPosition);
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i);
            if (timestamp >= approximatelyTimestamp) {
                return Math.max(0, i - 2); // it's a hack. Must be Math.max(0, i - 1)
            }
        }
        return 0;
    }

    @Override
    public float getTimestampRelPositionAt(int index) {
        long timestamp = timestamps.get(index);
        long minTimestamp = timestamps.get(0);
        long maxTimestamp = timestamps.get(timestamps.size() - 1);
        return ((float) (timestamp - minTimestamp)) / (maxTimestamp - minTimestamp);
    }

    @Override
    public int getTimestampIndex(long timestamp) {
        return timestamps.indexOf(timestamp);
    }

    @Override
    public float getClosestTimestampPosition(float toXPosition) {
        long minAxis = timestamps.get(0);
        long maxAxis = timestamps.get(timestamps.size() - 1);
        //long desiredAxis = (minAxis + (long) ((maxAxis - minAxis) * timestampRel)) + 1;
        float approximatelyDesiredAxis = (minAxis + ((maxAxis - minAxis) * toXPosition));
        for (int i = 0; i < timestamps.size(); i++) {
            long axis = timestamps.get(i);
            if (axis > approximatelyDesiredAxis) {
                float next = ((float) (axis - minAxis)) / (maxAxis - minAxis);
                if (i > 0) {
                    long previousAxis = timestamps.get(i - 1);
                    float previous = ((float) (previousAxis - minAxis)) / (maxAxis - minAxis);
                    if (Math.abs(previous - toXPosition) < Math.abs(next - toXPosition)) {
                        return previous;
                    } else {
                        return next;
                    }
                } else {
                    return next;
                }
            }
        }
        throw new IllegalArgumentException("Invalid timestamp rel: " + toXPosition);
    }

    @Override
    public long getClosestTimestamp(float toXPosition) {
        long minAxis = timestamps.get(0);
        long maxAxis = timestamps.get(timestamps.size() - 1);
        //long desiredAxis = (minAxis + (long) ((maxAxis - minAxis) * timestampRel)) + 1;
        float approximatelyDesiredAxis = (minAxis + ((maxAxis - minAxis) * toXPosition));
        for (int i = 0; i < timestamps.size(); i++) {
            long axis = timestamps.get(i);
            if (axis > approximatelyDesiredAxis) {
                float next = ((float) (axis - minAxis)) / (maxAxis - minAxis);
                if (i > 0) {
                    long previousAxis = timestamps.get(i - 1);
                    float previous = ((float) (previousAxis - minAxis)) / (maxAxis - minAxis);
                    if (Math.abs(previous - toXPosition) < Math.abs(next - toXPosition)) {
                        return previousAxis;
                    } else {
                        return axis;
                    }
                } else {
                    return axis;
                }
            }
        }
        throw new IllegalArgumentException("Invalid timestamp rel: " + toXPosition);
    }

    // finds min value for the given timestamp
    private int findMinValueAt(int index) {
        int min = Integer.MAX_VALUE;
        for (LineHolder holder : lineHolders) {
            if (!holder.visible) continue;

            int value = holder.data.getValueAt(index);
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    // finds max value for the given timestamp
    private int findMaxValueAt(int index) {
        int max = Integer.MIN_VALUE;
        for (LineHolder holder : lineHolders) {
            if (!holder.visible) continue;

            int value = holder.data.getValueAt(index);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private int getMinValueAt(int index) {
        return localMinimums.get(index, DEFAULT_MIN_VALUE);
    }

    private int getMaxValueAt(int index) {
        return localMaximums.get(index, DEFAULT_MAX_VALUE);
    }

    @Override
    public int getLocalMinimum(float fromXAxisRel, float toXAxisRel) {
        long startTimestamp = timestamps.get(0);
        long stopTimestamp = timestamps.get(timestamps.size() - 1);
        long fromTimestamp = (long) (startTimestamp + (stopTimestamp - startTimestamp) * fromXAxisRel) - 1;
        long toTimestamp = (long) (startTimestamp + (stopTimestamp - startTimestamp) * toXAxisRel) + 1;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i);
            if (timestamp < fromTimestamp) {
                if (i < timestamps.size() - 1) {
                    // check if the next axis is in the bounds
                    long nextTimestamp = timestamps.get(i + 1);
                    if (nextTimestamp >= fromTimestamp) {
                        int localMin = getMinValueAt(i);
                        if (localMin < min) {
                            min = localMin;
                        }
                    }
                }
                continue;
            }
            if (timestamp > toTimestamp) {
                int localMin = getMinValueAt(i);
                if (localMin < min) {
                    min = localMin;
                }
                break;
            }
            int localMin = getMinValueAt(i);
            if (localMin < min) {
                min = localMin;
            }
        }
        return min;
    }

    @Override
    public int getLocalMaximum(float fromXAxisRel, float toXAxisRel) {
        long startTimestamp = timestamps.get(0);
        long stopTimestamp = timestamps.get(timestamps.size() - 1);
        long fromTimestamp = (long) (startTimestamp + (stopTimestamp - startTimestamp) * fromXAxisRel) - 1;
        long toTimestamp = (long) (startTimestamp + (stopTimestamp - startTimestamp) * toXAxisRel) + 1;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i);
            if (timestamp < fromTimestamp) {
                if (i < timestamps.size() - 1) {
                    // check if the next axis is in the bounds
                    long nextTimestamp = timestamps.get(i + 1);
                    if (nextTimestamp >= fromTimestamp) {
                        int localMax = getMaxValueAt(i);
                        if (localMax > max) {
                            max = localMax;
                        }
                    }
                }
                continue;
            }
            if (timestamp > toTimestamp) {
                int localMax = getMaxValueAt(i);
                if (localMax > max) {
                    max = localMax;
                }
                break;
            }
            int localMax = getMaxValueAt(i);
            if (localMax > max) {
                max = localMax;
            }
        }
        return max;
    }

    @Override
    public int getLineCount() {
        return lineHolders.size();
    }

    @Override
    public Line getLineAt(int index) {
        return lineHolders.get(index).data;
    }

    @Override
    public boolean isLineEnabled(Line chart) {
        for (int i = 0; i < lineHolders.size(); i++) {
            LineHolder holder = lineHolders.get(i);
            if (holder.data.equals(chart)) {
                return holder.visible;
            }
        }
        return false;
    }

    @Override
    public void setLineEnabled(Line chart, boolean visible) {
        for (int i = 0; i < lineHolders.size(); i++) {
            LineHolder holder = lineHolders.get(i);
            if (holder.data.equals(chart)) {
                holder.visible = visible;
            }
        }
        // TO DO: think of much more optimized way
        calcMinimumsAndMaximums();
    }

    @Override
    public String getYStampText(int value) {
        return String.valueOf(value);
    }

    @Override
    public String getXStampTextAt(int index) {
        return timestampTexts.get(index);
    }

    @Override
    public String toString() {
        return "Line chart #" + String.valueOf(id);
    }
}
