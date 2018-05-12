package socialite.yarn;

import mpi.MPI;
import mpi.MPIException;
import socialite.dist.PortMap;
import socialite.util.BitUtils;
import socialite.util.SociaLiteException;

/**
 * Created by jiwon on 9/10/15.
 */
public class ClusterConf {
    private static ClusterConf inst = new ClusterConf();

    public static ClusterConf get() {
        return inst;
    }

    private final int maxNumWorkers;
    private int numWorkers;
    private int numWorkerThreads;
    private int workerHeapSize;

    private ClusterConf() {
        numWorkers = Integer.parseInt(System.getProperty("socialite.worker.num", "2"));
        String numCores = "" + Runtime.getRuntime().availableProcessors();
        numWorkerThreads = Integer.parseInt(System.getProperty("socialite.worker.num_threads", numCores));
        workerHeapSize = Integer.parseInt(System.getProperty("socialite.worker.heap_size", "8192"));

        int max = Integer.parseInt(System.getProperty("socialite.worker.max", "-1"));
        max = Math.max(max, numWorkers * 4);
        maxNumWorkers = BitUtils.nextHighestPowerOf2(max);
//        nextHighestPowerOf2
//        0	0
//        1	1
//        2	2
//        3	4
//        4	4
//        5	8
//        6	8
//        7	8
//        8	8
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public int getMaxNumWorkers() {
        return maxNumWorkers;
    }

    public int getNumWorkerThreads() {
        return numWorkerThreads;
    }

    public int getWorkerHeapSize() {
        return workerHeapSize;
    }

    public String getHost() {
//        return NetUtils.getHostname().split("/")[1];
        return System.getProperty("socialite.master", "localhost");
    }

    public int getPort(String proto) {
        int basePort = PortMap.DEFAULT_BASE_PORT;
        String _basePort = System.getProperty("socialite.port");
        if (_basePort != null) basePort = Integer.parseInt(_basePort);
        switch (proto) {
            case "query":
                return basePort;
            case "workerReq":
                return basePort + 1;
            case "tupleReq":
                return basePort + 2;
            default:
                throw new SociaLiteException("Cannot find port for " + proto);
        }
    }

    public int getRank() {
        if (System.getenv("OMPI_UNIVERSE_SIZE") != null) {
            try {
                return MPI.COMM_WORLD.getRank();
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }
        throw new SociaLiteException("not in mpi env");
    }
}
