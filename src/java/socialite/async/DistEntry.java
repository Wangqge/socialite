package socialite.async;

import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.dist.MsgType;
import socialite.async.dist.master.AsyncMaster;
import socialite.async.dist.worker.AsyncWorker;
import socialite.async.util.NetworkThread;
import socialite.async.util.TextUtils;
import socialite.dist.master.MasterNode;
import socialite.dist.worker.WorkerNode;
import socialite.util.SociaLiteException;
import socialite.yarn.ClusterConf;

import java.io.IOException;
import java.util.stream.IntStream;

public class DistEntry {
    private static final Log L = LogFactory.getLog(DistEntry.class);

    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException, MPIException, IOException {
//z3
//        System.out.println(System.getProperty("java.library.path"));
//        prototest pro=new prototest();
//        pro.test();
        if (System.getenv("OMPI_UNIVERSE_SIZE") == null) {
            throw new RuntimeException("Run me by mpirun");
        }
        MPI.Init(args);
        int machineNum = MPI.COMM_WORLD.getSize();
        int machineId = MPI.COMM_WORLD.getRank();
        int workerNum = machineNum - 1;
        L.info("Machine " + machineId + " Xmx " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
        if (machineNum - 1 != ClusterConf.get().getNumWorkers())
            throw new SociaLiteException(String.format("MPI Workers (%d)!= Socialite Workers (%d)", workerNum, ClusterConf.get().getNumWorkers()));
        if (machineId == 0) {
            AsyncConfig.parse(TextUtils.readText(args[0]));
            L.info("master started");
            MasterNode.startMasterNode();
            AsyncMaster asyncMaster = new AsyncMaster(AsyncConfig.get().getDatalogProg());
            //L.info(AsyncConfig.get().getDatalogProg().toString());
            asyncMaster.startMaster();
            IntStream.rangeClosed(1, workerNum).forEach(dest ->
                    NetworkThread.get().send(new byte[1], dest, MsgType.EXIT.ordinal())
            );
        } else {
            L.info("Worker Started " + machineId);
            WorkerNode.startWorkerNode();
            AsyncWorker worker = new AsyncWorker();
            worker.startWorker();
            NetworkThread.get().read(0, MsgType.EXIT.ordinal());
        }
        NetworkThread.get().shutdown();
        NetworkThread.get().join();
        MPI.Finalize();
        L.info("process " + machineId + " exit.");
        System.exit(0);
    }

}
