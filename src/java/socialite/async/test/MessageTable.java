package socialite.async.test;

import socialite.async.atomic.MyAtomicDouble;
import socialite.async.atomic.MyAtomicInteger;
import socialite.async.codegen.MessageTableBase;
import socialite.async.util.SerializeTool;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageTable extends MessageTableBase {
    private Map<Integer, MyAtomicDouble> keyDeltaMap;
    public static final double IDENTITY_ELEMENT = 0;

    public MessageTable() {
        keyDeltaMap = new ConcurrentHashMap<>();
    }

    public void apply(int key, double delta) {
        MyAtomicDouble atomicDelta = keyDeltaMap.putIfAbsent(key, new MyAtomicDouble(delta));
        if (atomicDelta != null) {
            atomicDelta.accumulateAndGet(delta, Double::sum);
        }
        updateCounter.addAndGet(1);
    }

    @Override
    public void resetDelta() {
        //keyDeltaMap.values().forEach(delta -> delta.set(IDENTITY_ELEMENT));
        keyDeltaMap.clear();
        updateCounter.set(0);
    }

    @Override
    public Map<Integer, MyAtomicDouble> getIntegerDoubleMap() {
        return keyDeltaMap;
    }

    @Override
    public int size() {
        return keyDeltaMap.size();
    }

    @Override
    public double accumulate() {
        return keyDeltaMap.values().stream().map(MyAtomicDouble::get)
                .filter(val -> val != IDENTITY_ELEMENT).reduce(0.0d, Double::sum) + 0.0;
    }

}

//Idea: Copy MessageTable -> two arrays, and serialize two arrays -> send
class Main {
    public static void main(String[] args) {
        MessageTableBase sendableMessageTable = new MessageTable();


//        for (int i = 0; i < 1000 * 100; i++) {
//            sendableMessageTable.apply(i, i + 0.0);
//        }

//        for (int i = 0; i < 4; i++) {
//            new Thread(() -> {
//                SerializeTool serializeTool = new SerializeTool.Builder()
//
//                        .registry(Integer.class,0)
////                        .registry(Long.class)
////                        .registry(Float.class)
////                        .registry(Double.class)
////                        .registry(MyAtomicInteger.class)
////                        .registry(MyAtomicLong.class)
////                        .registry(MyAtomicFloat.class)
//                        .registry(MyAtomicDouble.class,1)
//
//                        .registry(ConcurrentHashMap.class,2)
//                        .registry(MessageTable.class,3)
//                        .setSerializeTransient(true) //!!!!!!!!!!AtomicDouble's value field is transient
//                        .build();
////                SerializeTool serializeTool = new SerializeTool.Builder().build();
//                StopWatch stopWatch = new StopWatch();
//                stopWatch.start();
//                ByteBuffer buffer = serializeTool.toByteBuffer(2048 + sendableMessageTable.size() * (8 + 8), sendableMessageTable);
////            sendableMessageTable.resetDelta();
//                stopWatch.stop();
//                buffer.flip();
//                MessageTable messageTable = serializeTool.fromByteBuffer(buffer, MessageTable.class);
//
//                System.out.println(String.format("Serialize Time:%d buff size:%d msg size:%d val:%f", stopWatch.getTime(), buffer.position(), messageTable.size(), messageTable.accumulate()));
//            }).start();
//        }
    }
}

