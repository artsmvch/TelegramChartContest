package com.froloapp.telegramchart.widget.linechartview.factory;


import com.froloapp.telegramchart.widget.linechartview.Line;

import java.util.List;

public final class Lines {
    private Lines() { }

    private static abstract class AbsLine implements Line {
        final int color;
        final String name;
        AbsLine(int color, String name) {
            this.color = color;
            this.name = name;
        }
        @Override public int getColor() {
            return color;
        }
        @Override public String getName() {
            return name;
        }
    }

    private static class IndexedData extends AbsLine {
        final List<Integer> values;
        IndexedData(List<Integer> values, int color, String name) {
            super(color, name);
            this.values = values;
        }
        @Override public int getValueAt(int index) {
            return values.get(index);
        }
    }

    public static Line create(List<Integer> values, int color, String name) {
        return new IndexedData(values, color, name);
    }
}