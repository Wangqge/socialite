package socialite.async.codegen;

//AsyncRuntime(initSize, threadNum, dynamic, checkType, checkerInterval, threshold, cond) ::= <<

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.AsyncConfig;
import socialite.async.util.ResettableCountDownLatch;
import socialite.tables.TableInst;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public abstract class BaseAsyncRuntime implements Runnable {
    protected static final Log L = LogFactory.getLog(BaseAsyncRuntime.class);
    protected AsyncConfig asyncConfig;
    private volatile boolean stop;
    protected ComputingThread[] computingThreads;
    protected SchedulerThread schedulerThread;
    protected CheckThread checkerThread;
    protected BaseAsyncTable asyncTable;
    protected AtomicInteger updateCounter;
    private CyclicBarrier barrier;
    private ResettableCountDownLatch countDownLatch;
    private static final int SAMPLE_SIZE = 1000;
    private volatile double globalThreshold;

    private volatile boolean check;
    private final Object lock = new Object();
    private long lastCheckTime;

    protected abstract boolean loadData(TableInst[] initTableInstArr, TableInst[] edgeTableInstArr, TableInst[] extraTableInstArr);

    protected void createThreads() {
        if(checkerThread==null)
            throw new AssertionError("create CheckThread before call createThreads");
        asyncConfig = AsyncConfig.get();
        updateCounter = new AtomicInteger();
        globalThreshold = -Double.MAX_VALUE;
        computingThreads = new ComputingThread[asyncConfig.getThreadNum()];
        IntStream.range(0, asyncConfig.getThreadNum()).forEach(i -> computingThreads[i] = new ComputingThread(i));
        if (asyncConfig.getEngineType() != AsyncConfig.EngineType.ASYNC)// For Sync/Semi-Async mode, the barrier is required
            barrier = new CyclicBarrier(asyncConfig.getThreadNum(), checkerThread);
        //For global priority mode, update the global priority when the half threads finish a round
        if (asyncConfig.getPriorityType() == AsyncConfig.PriorityType.GLOBAL) {
            int half = asyncConfig.getThreadNum() / 2;
            countDownLatch = new ResettableCountDownLatch(half == 0 ? 1 : half);
            schedulerThread = new SchedulerThread();
        }
    }

    public BaseAsyncTable getAsyncTable() {
        return asyncTable;
    }

    protected class ComputingThread extends Thread {
        private final int[] bound;
        private int tid;
        double[] deltaSample;
        final double SCHEDULE_PORTION;
        private AsyncConfig asyncConfig;
        private ThreadLocalRandom randomGenerator;

        public ComputingThread(int tid) {
            this.tid = tid;
            asyncConfig = AsyncConfig.get();
            randomGenerator = ThreadLocalRandom.current();
            SCHEDULE_PORTION = asyncConfig.getSchedulePortion();
            deltaSample = new double[SAMPLE_SIZE];
            bound = new int[2];
        }


        @Override
        public void run() {
            if (tid == 0) lastCheckTime = System.currentTimeMillis();
            try {
                while (!stop) {
                    int start;
                    int end;
                    synchronized (bound) {
                        start = bound[0];
                        end = bound[1];
                    }

                    if (start == end) { //empty thread, sleep to reduce CPU race
                        Thread.sleep(10);
                        if (asyncConfig.getEngineType() != AsyncConfig.EngineType.ASYNC) //Sync or Semi-Async Mode
                            barrier.await();
                        continue;
                    }

                    if (asyncConfig.getPriorityType() == AsyncConfig.PriorityType.NONE) {
                        for (int k = start; k < end; k++) {
                            if (asyncConfig.getEngineType() == AsyncConfig.EngineType.SYNC) {
                                if (asyncTable.updateLockFree(k, checkerThread.iter)) updateCounter.addAndGet(1);
                            } else {
                                if (asyncTable.updateLockFree(k)) updateCounter.addAndGet(1);
                            }
                        }
                    } else if (asyncConfig.getPriorityType() == AsyncConfig.PriorityType.LOCAL) {
                        for (int i = 0; i < deltaSample.length; i++) {
                            int ind = randomGenerator.nextInt(start, end);
                            deltaSample[i] = asyncTable.getPriority(ind);
                        }
//                            boolean update = false;
//                            for (double delta : deltaSample)
//                                if (delta != 0) {
//                                    update = true;
//                                    break;
//                                }
//                            if (!update) {
////                                L.info("got it");
//                                Thread.sleep(1);
//                                continue;
//                            }
                        Arrays.sort(deltaSample);
                        int cutIndex = (int) (deltaSample.length * (1 - SCHEDULE_PORTION));
                        double threshold = deltaSample[cutIndex];

                        for (int k = start; k < end; k++) {
                            if (asyncTable.getPriority(k) >= threshold) {
                                if (asyncTable.updateLockFree(k))
                                    updateCounter.addAndGet(1);
                            }
                        }
                    } else if (asyncConfig.getPriorityType() == AsyncConfig.PriorityType.GLOBAL) {
                        for (int k = start; k < end; k++) {
                            if (asyncTable.getPriority(k) >= globalThreshold) {
                                if (asyncTable.updateLockFree(k)) updateCounter.addAndGet(1);
                            }
                        }
                        countDownLatch.countDown();
                    }
                    if (asyncConfig.getEngineType() == AsyncConfig.EngineType.ASYNC) {
                        if (System.currentTimeMillis() - lastCheckTime >= checkerThread.CHECKER_INTERVAL) {
                            checkerThread.notifyCheck();
                            lastCheckTime = System.currentTimeMillis();
                        }
                    } else {
                        barrier.await();
                    }
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            String str;
            synchronized (bound) {
                str = String.format("id: %d range: [%d, %d)", tid, bound[0], bound[1]);
            }
            return str;
        }
    }

    protected void arrangeTask() {
        int threadNum = AsyncConfig.get().getThreadNum();
        int size = asyncTable.getSize();
        int blockSize = size / threadNum;
        if (blockSize == 0) {
//                L.warn("too many threads asynctable size " + size);
            blockSize = size;
        }

        for (int tid = 0; tid < threadNum; tid++) {
            int start = tid * blockSize;
            int end = (tid + 1) * blockSize;
            if (tid == threadNum - 1)//last thread, assign all
                end = size;
            if (start >= size) {//assign empty tasks
                start = 0;
                end = 0;
            } else if (end > size || tid == threadNum - 1) {//block < lastThread's tasks or block > ~
                end = size;
            }

            int[] bound = computingThreads[tid].bound;
            synchronized (bound) {
                bound[0] = start;
                bound[1] = end;
            }
        }
    }


    protected class SchedulerThread extends Thread {
        AsyncConfig asyncConfig;
        private ThreadLocalRandom randomGenerator;
        double[] deltaSample;
        final double SCHEDULE_PORTION;

        SchedulerThread() {
            asyncConfig = AsyncConfig.get();
            randomGenerator = ThreadLocalRandom.current();
            SCHEDULE_PORTION = asyncConfig.getSchedulePortion();
            deltaSample = new double[SAMPLE_SIZE];
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int size = asyncTable.getSize();


                for (int i = 0; i < deltaSample.length; i++) {
                    int ind = randomGenerator.nextInt(0, size);
                    deltaSample[i] = asyncTable.getPriority(ind);
                }

//                boolean update = false;
//                for (double delta : deltaSample)
//                    if (delta != 0) {
//                        update = true;
//                        break;
//                    }
//                if (!update) {
//                    L.info("got it");
//                    priorityThreshold = Double.MAX_VALUE;
//                    continue;
//                }

                Arrays.sort(deltaSample);
                int cutIndex = (int) (deltaSample.length * (1 - SCHEDULE_PORTION));
                globalThreshold = deltaSample[cutIndex];
                countDownLatch.reset();
            }
        }
    }

    protected abstract class CheckThread extends Thread {
        protected StopWatch stopWatch;
        protected final int CHECKER_INTERVAL = AsyncConfig.get().getCheckInterval();
        protected int iter = 0;

        protected CheckThread() {
        }

        @Override
        public void run() {
            iter++;
            if (stopWatch == null) {
                stopWatch = new StopWatch();
                stopWatch.start();
            }
        }

        void waitingCheck() throws InterruptedException {
            synchronized (lock) {
                while (!check) {
                    lock.wait();
                }
            }
            check = false;
        }

        protected void done() {
            L.info("call done");
            stop = true;
            stopWatch.stop();
            L.info("UPDATE_TIMES:" + updateCounter.get());
            L.info("DONE ELAPSED:" + stopWatch.getTime());
            L.info("CHECKER THREAD EXITED");
        }

        void notifyCheck() {
            synchronized (lock) {
                check = true;
                lock.notify();
            }
        }

    }

    public static boolean eval(double val) {
        AsyncConfig asyncConfig = AsyncConfig.get();
        double threshold = asyncConfig.getThreshold();
        switch (asyncConfig.getCond()) {
            case G:
                return val > threshold;
            case GE:
                return val >= threshold;
            case E:
                return val == threshold;
            case LE:
                return val <= threshold;
            case L:
                return val < threshold;
        }
        throw new UnsupportedOperationException();
    }

}