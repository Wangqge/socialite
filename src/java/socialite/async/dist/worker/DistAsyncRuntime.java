package socialite.async.dist.worker;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.AsyncConfig;
import socialite.async.codegen.BaseAsyncRuntime;
import socialite.async.codegen.BaseDistAsyncTable;
import socialite.async.codegen.MessageTableBase;
import socialite.async.dist.MsgType;
import socialite.async.dist.Payload;
import socialite.async.util.NetworkThread;
import socialite.async.util.NetworkUtil;
import socialite.async.util.SerializeTool;
import socialite.parser.Table;
import socialite.resource.DistTablePartitionMap;
import socialite.resource.SRuntimeWorker;
import socialite.resource.TableInstRegistry;
import socialite.tables.TableInst;
import socialite.util.Loader;
import socialite.util.SociaLiteException;
import socialite.visitors.VisitorImpl;
import socialite.yarn.ClusterConf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DistAsyncRuntime extends BaseAsyncRuntime {
    private static final Log L = LogFactory.getLog(DistAsyncRuntime.class);
    private static DistAsyncRuntime inst;
    private final int myWorkerId;
    private final int workerNum;
    private NetworkThread networkThread;
    private Sender[] sender;
    private Receiver[] receiver;
    private Payload payload;
    private volatile boolean stopTransmitting;
    private static final int NUM_OF_SENDER_RECEIVER = 2;


    private DistAsyncRuntime() {
        workerNum = ClusterConf.get().getNumWorkers();
        myWorkerId = ClusterConf.get().getRank() - 1;
        networkThread = NetworkThread.get();
        networkThread.start();
    }

    public static synchronized DistAsyncRuntime getInst() {
        if (inst == null)
            inst = new DistAsyncRuntime();
        return inst;
    }

    @Override
    public void run() {
        waitingCmd();//waiting for AsyncConfig
        SRuntimeWorker runtimeWorker = SRuntimeWorker.getInst();
        TableInstRegistry tableInstRegistry = runtimeWorker.getTableRegistry();
        Map<String, Table> tableMap = runtimeWorker.getTableMap();
        TableInst[] initTableInstArr = tableInstRegistry.getTableInstArray(tableMap.get(payload.getRecTableName()).id());
        TableInst[] edgeTableInstArr = tableInstRegistry.getTableInstArray(tableMap.get(payload.getEdgeTableName()).id());
        TableInst[] extraTableInstArr = null;
        if (tableMap.get(payload.getExtraTableName()) != null) {
            extraTableInstArr = tableInstRegistry.getTableInstArray(tableMap.get(payload.getExtraTableName()).id());
        }
        if (loadData(initTableInstArr, edgeTableInstArr, extraTableInstArr)) {//this worker is idle, stop
            clearData();
            createThreads();
            startThreads();
        } else {//this worker is idle
            throw new SociaLiteException("Worker " + myWorkerId + " is idle, please reduce the number of workers");
        }
    }

    private void waitingCmd() {
        SerializeTool serializeTool = new SerializeTool.Builder().build();
        byte[] data = networkThread.read(0, MsgType.NOTIFY_INIT.ordinal());
        payload = serializeTool.fromBytes(data, Payload.class);
        AsyncConfig.set(payload.getAsyncConfig());
        L.info("RECV CMD NOTIFY_INIT CONFIG:" + AsyncConfig.get());
    }

    void clearData() {
        networkThread.send(new byte[1], 0, MsgType.CLEAR_DATA.ordinal());
        networkThread.read(0, MsgType.CLEAR_DATA_DONE.ordinal());
    }

    @Override
    protected boolean loadData(TableInst[] initTableInstArr, TableInst[] edgeTableInstArr, TableInst[] extraTableInstArr) {
        Loader.loadFromBytes(payload.getByteCodes());
        Class<?> messageTableClass = Loader.forName("socialite.async.codegen.MessageTable");
        Class<?> distAsyncTableClass = Loader.forName("socialite.async.codegen.DistAsyncTable");
        try {
            SRuntimeWorker runtimeWorker = SRuntimeWorker.getInst();

            DistTablePartitionMap partitionMap = runtimeWorker.getPartitionMap();
            int indexForTableId = runtimeWorker.getTableMap().get(payload.getEdgeTableName()).id();
            Constructor constructor = distAsyncTableClass.getConstructor(messageTableClass.getClass(), DistTablePartitionMap.class, int.class);

            asyncTable = (BaseDistAsyncTable) constructor.newInstance(messageTableClass, partitionMap, indexForTableId);

            for (TableInst tableInst : edgeTableInstArr) {
                Method method = tableInst.getClass().getDeclaredMethod("iterate", VisitorImpl.class);
                if (!tableInst.isEmpty()) {
                    method.invoke(tableInst, asyncTable.getEdgeVisitor());
                    tableInst.clear();
                }
            }

            for (TableInst tableInst : initTableInstArr) {
                Method method = tableInst.getClass().getDeclaredMethod("iterate", VisitorImpl.class);
                if (!tableInst.isEmpty()) {
                    method.invoke(tableInst, asyncTable.getInitVisitor());
                    tableInst.clear();
                }
            }

            if (extraTableInstArr != null) {
                for (TableInst tableInst : extraTableInstArr) {
                    Method method = tableInst.getClass().getDeclaredMethod("iterate", VisitorImpl.class);
                    if (!tableInst.isEmpty()) {
                        method.invoke(tableInst, asyncTable.getExtraVisitor());
                        tableInst.clear();
                    }
                }
            }
            System.gc();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e1) {
            e1.printStackTrace();
        }


        System.gc();
        L.info("WorkerId " + myWorkerId + " Data Loaded size:" + asyncTable.getSize());
        return true;
    }

    @Override
    protected void createThreads() {
        checkerThread = new CheckThread();
        super.createThreads();
        arrangeTask();
        if (asyncConfig.getEngineType() == AsyncConfig.EngineType.ASYNC) {
            createSenderReceiver();
        }
    }

    private void createSenderReceiver() {
        sender = new Sender[NUM_OF_SENDER_RECEIVER];
        receiver = new Receiver[NUM_OF_SENDER_RECEIVER];
        int blockSize = workerNum / NUM_OF_SENDER_RECEIVER;
        assert blockSize > 0;
        if (blockSize == 0) blockSize = 1;

        for (int i = 0; i < NUM_OF_SENDER_RECEIVER; i++) {
            int start = i * blockSize;
            int end = (i + 1) * blockSize;
            if (i == NUM_OF_SENDER_RECEIVER - 1)
                end = workerNum;
            sender[i] = new Sender(start, end);
            receiver[i] = new Receiver(start, end);
        }
    }

    private void startThreads() {
        if (AsyncConfig.get().getPriorityType() == AsyncConfig.PriorityType.GLOBAL)
            schedulerThread.start();
        if (asyncConfig.getEngineType() == AsyncConfig.EngineType.ASYNC) {
            Arrays.stream(sender).forEach(Thread::start);
            Arrays.stream(receiver).forEach(Thread::start);
            L.info("network thread started");
            checkerThread.start();
            L.info("checker started");
        }

        Arrays.stream(computingThreads).filter(Objects::nonNull).forEach(Thread::start);
        L.info(String.format("Worker %d all threads started.", myWorkerId));
        try {
            for (ComputingThread computingThread : computingThreads) computingThread.join();
            L.info("Worker " + myWorkerId + " Computing Threads exited.");

            if (asyncConfig.getEngineType() == AsyncConfig.EngineType.ASYNC) {
                checkerThread.join();
                L.info("CheckThread exited");
                Arrays.stream(sender).forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                L.info(String.format("%d SenderThread exited", myWorkerId));
                Arrays.stream(receiver).forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                L.info(String.format("%d RecvThread exited", myWorkerId));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Sender extends Thread {
        private SerializeTool serializeTool;
        private int workerIdFrom;
        private int workerIdTo;

        private Sender(int workerIdFrom, int workerIdTo) {
            serializeTool = new SerializeTool.Builder()
                    .setSerializeTransient(true) //!!!!!!!!!!AtomicDouble's value field is transient
                    .build();
            this.workerIdFrom = workerIdFrom;
            this.workerIdTo = workerIdTo;
        }

        @Override
        public void run() {
            try {
                while (!stopTransmitting) {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    for (int sendToWorkerId = workerIdFrom; sendToWorkerId < workerIdTo; sendToWorkerId++) {
                        ByteBuffer buffer;
                        if (sendToWorkerId == myWorkerId) continue;
                        if (asyncConfig.isMVCC())
                            buffer = ((BaseDistAsyncTable) asyncTable).getSendableMessageTableByteBufferMVCC(sendToWorkerId, serializeTool);
                        else
                            buffer = ((BaseDistAsyncTable) asyncTable).getSendableMessageTableByteBuffer(sendToWorkerId, serializeTool);

                        if (stopTransmitting) return;
                        networkThread.send(buffer, sendToWorkerId + 1, MsgType.MESSAGE_TABLE.ordinal());
                    }
                    stopWatch.stop();
                    if (asyncConfig.getEngineType() != AsyncConfig.EngineType.ASYNC) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver extends Thread {
        private SerializeTool serializeTool;
        private Class<?> messageTableClass;
        private int workerIdFrom;
        private int workerIdTo;

        private Receiver(int workerIdFrom, int workerIdTo) {
            messageTableClass = Loader.forName("socialite.async.codegen.MessageTable");
            serializeTool = new SerializeTool.Builder()
                    .setSerializeTransient(true)
                    .build();
            this.workerIdFrom = workerIdFrom;
            this.workerIdTo = workerIdTo;
        }

        @Override
        public void run() {
            while (!stopTransmitting) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                for (int recvFromWorkerId = workerIdFrom; recvFromWorkerId < workerIdTo; recvFromWorkerId++) {
                    if (recvFromWorkerId == myWorkerId) continue;
                    ByteBuffer buffer;
                    while ((buffer = networkThread.tryReadByteBuffer(recvFromWorkerId + 1, MsgType.MESSAGE_TABLE.ordinal())) == null) {
                        if (stopTransmitting)
                            return;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    MessageTableBase messageTable = (MessageTableBase) serializeTool.fromByteBuffer(buffer, messageTableClass);
                    if (messageTable==null){
                        System.out.println("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
                    }
                    ((BaseDistAsyncTable) asyncTable).applyBuffer(messageTable);
                }
                stopWatch.stop();
                if (asyncConfig.getEngineType() != AsyncConfig.EngineType.ASYNC) break;
            }
        }
    }

    private class CheckThread extends BaseAsyncRuntime.CheckThread {
        private AsyncConfig asyncConfig;
        private SerializeTool serializeTool;


        private CheckThread() {
            asyncConfig = AsyncConfig.get();
            serializeTool = new SerializeTool.Builder().build();
        }

        @Override
        public void run() {
            super.run();
            boolean[] feedback = new boolean[1];
            while (true) {
                if (asyncConfig.isDynamic())
                    arrangeTask();
                long[] rxTx = new long[]{0, 0};
                if (asyncConfig.isNetworkInfo())
                    rxTx = NetworkUtil.getNetwork();
                if (asyncConfig.getEngineType() != AsyncConfig.EngineType.ASYNC) {//sync mode
                    sendAndWait();
                    double partialSum = aggregate();
                    double[] data = new double[]{partialSum, updateCounter.get(), rxTx[0], rxTx[1]};
                    networkThread.send(serializeTool.toBytes(data), 0, MsgType.REQUIRE_TERM_CHECK.ordinal());
                    byte[] feedbackData = networkThread.read(0, MsgType.TERM_CHECK_FEEDBACK.ordinal());
                    feedback = serializeTool.fromBytes(feedbackData, feedback.getClass());
                    if (feedback[0]) {
                        flush();
                    }
                    break;//exit function, run will be called next round
                } else {
                    networkThread.read(0, MsgType.REQUIRE_TERM_CHECK.ordinal());
                    double partialSum = aggregate();
                    double[] data = new double[]{partialSum, updateCounter.get(), rxTx[0], rxTx[1]};
                    networkThread.send(serializeTool.toBytes(data), 0, MsgType.TERM_CHECK_PARTIAL_VALUE.ordinal());
                    byte[] feedbackData = networkThread.read(0, MsgType.TERM_CHECK_FEEDBACK.ordinal());
                    feedback = serializeTool.fromBytes(feedbackData, feedback.getClass());

                    if (feedback[0]) {
                        flush();
                        break;
                    }
                }
            }
        }

        private void sendAndWait() {
            createSenderReceiver();
            Arrays.stream(sender).forEach(Thread::start);
            Arrays.stream(receiver).forEach(Thread::start);
            Arrays.stream(sender).forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Arrays.stream(receiver).forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        private void flush() {
            done();
            L.info("waiting for flush");
            try {
                Thread.sleep(asyncConfig.getMessageTableWaitingInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopTransmitting = true;
            L.info("flushed");
        }

        private double aggregate() {
            double partialSum = 0;
            BaseDistAsyncTable baseDistAsyncTable = (BaseDistAsyncTable) asyncTable;

            if (asyncTable != null) {//null indicate this worker is idle
                if (asyncConfig.getCheckType() == AsyncConfig.CheckerType.DELTA || asyncConfig.getCheckType() == AsyncConfig.CheckerType.DIFF_DELTA) {
                    partialSum = asyncTable.accumulateDelta();

                    for (int workerId = 0; workerId < workerNum; workerId++) {
                        if (workerId == myWorkerId) continue;
                        //to check the termination , the message also should be considered
                        if (asyncConfig.isMVCC()) {
                            MessageTableBase messageTable1 = baseDistAsyncTable.getMessageTableListPair()[workerId][0];
                            MessageTableBase messageTable2 = baseDistAsyncTable.getMessageTableListPair()[workerId][1];
                            partialSum += messageTable1.accumulate();
                            partialSum += messageTable2.accumulate();
                        } else {
                            partialSum += baseDistAsyncTable.getMessageTableList()[workerId].accumulate();
                        }
                    }
                } else if (asyncConfig.getCheckType() == AsyncConfig.CheckerType.VALUE || asyncConfig.getCheckType() == AsyncConfig.CheckerType.DIFF_VALUE) {
                    partialSum = asyncTable.accumulateValue();
                }
            }
            return partialSum;
        }

    }


}