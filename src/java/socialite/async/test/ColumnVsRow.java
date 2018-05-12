package socialite.async.test;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import socialite.async.atomic.MyAtomicDouble;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColumnVsRow {
    public static final String DATA_SET = "/home/gengl/Datasets/wikipedia_link_en/edge_pair.txt";

    public static void main(String[] args) throws IOException {
        testRowMode();
//        testColMode();
    }

    static void testRowMode() throws IOException {
        RowMode rowMode = new RowMode();
        BufferedReader reader = new BufferedReader(new FileReader(DATA_SET));
        String line;
        long start = Runtime.getRuntime().totalMemory();
        long counter = 0;
        while ((line = reader.readLine()) != null) {
            String[] tmp = line.split("\\s+");
            int src = Integer.parseInt(tmp[0]);
            int dst = Integer.parseInt(tmp[1]);
            rowMode.addEntry(src, 0, 0.2, dst);
            counter++;
            if (counter % 1000000 == 0)
                System.out.println(String.format("Usage:%d MB", (Runtime.getRuntime().totalMemory() - start) / 1024 / 1024));
        }
        reader.close();
        rowMode.flush();
        System.out.println(String.format("Usage:%d MB", (Runtime.getRuntime().freeMemory() - start) / 1024 / 1024));
    }

    static void testColMode() throws IOException {
        ColMode colMode = new ColMode();
        BufferedReader reader = new BufferedReader(new FileReader(DATA_SET));
        String line;
        long start = Runtime.getRuntime().totalMemory();
        long counter = 0;
        while ((line = reader.readLine()) != null) {
            String[] tmp = line.split("\\s+");
            int src = Integer.parseInt(tmp[0]);
            int dst = Integer.parseInt(tmp[1]);
            colMode.addEntry(src, 0, 0.2, dst);
            counter++;
            if (counter % 1000000 == 0)
                System.out.println(String.format("Usage:%d MB", (Runtime.getRuntime().totalMemory() - start) / 1024 / 1024));
        }
        reader.close();
        colMode.flush();
        System.out.println(String.format("Usage:%d MB", (start - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

    }
}

class ColMode {
    TIntList index;
    TDoubleList value;
    List<MyAtomicDouble> delta;
    List<int[]> adjacency;
    int last = -1;
    TIntArrayList dstList = new TIntArrayList();

    ColMode() {
        index = new TIntArrayList();
        value = new TDoubleArrayList();
        delta = new ArrayList<>();
        adjacency = new ArrayList<>();
    }

    double lastValue;
    double lastDelta;

    public void addEntry(int index, double value, double delta, int dst) {
        if (last != -1 && index != last) {
            this.index.add(last);
            this.value.add(value);
            this.delta.add(new MyAtomicDouble(delta));
            this.adjacency.add(dstList.toArray());
            dstList.clear();
        } else {
            dstList.add(dst);
        }
        last = index;
        lastValue = value;
        lastDelta = delta;
    }

    public void flush() {
        this.index.add(last);
        this.value.add(lastValue);
        this.delta.add(new MyAtomicDouble(lastDelta));
        this.adjacency.add(dstList.toArray());
        dstList.clear();
    }
}

class RowMode {
    class Row {
        int index;
        double value;
        MyAtomicDouble delta;
        int[] adjacency;

        public Row(int index, double value, double delta, int[] adjacency) {
            this.index = index;
            this.value = value;
            this.delta = new MyAtomicDouble(delta);
            this.adjacency = adjacency;
        }
    }

    List<Row> entries;

    RowMode() {
        entries = new ArrayList<>();
    }

    int last = -1;
    TIntList dstList = new TIntArrayList();
    double lastValue;
    double lastDelta;

    public void addEntry(int index, double value, double delta, int dst) {
        if (last != -1 && index != last) {
            Row row = new Row(last, value, delta, dstList.toArray());
            entries.add(row);
            dstList.clear();
        } else {
            dstList.add(dst);
        }
        last = index;
        lastValue = value;
        lastDelta = delta;
    }

    public void flush() {
        Row row = new Row(last, lastValue, lastDelta, dstList.toArray());
        entries.add(row);
        dstList.clear();
    }
}