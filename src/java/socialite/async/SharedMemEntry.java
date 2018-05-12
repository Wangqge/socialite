package socialite.async;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.codegen.Pair;
import socialite.async.dist.master.AsyncMaster;
import socialite.async.dist.worker.AsyncWorker;
import socialite.async.engine.LocalAsyncEngine;
import socialite.async.util.TextUtils;
import socialite.dist.master.MasterNode;
import socialite.dist.worker.WorkerNode;
import socialite.util.SociaLiteException;
import socialite.yarn.ClusterConf;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SharedMemEntry {
    private static final Log L = LogFactory.getLog(SharedMemEntry.class);

    //-Dlog4j.configuration=file:/home/gengl/AsyncDatalog/conf/log4j.properties
    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException, MPIException {
        //System.out.println(args[0]);

        AsyncConfig.parse(TextUtils.readText(args[0]));
        AsyncConfig asyncConfig = AsyncConfig.get();
        LocalAsyncEngine localAsyncEngine = new LocalAsyncEngine(asyncConfig.getDatalogProg());
        localAsyncEngine.run();
    }

//    public static final MyVisitorImpl myVisitor = new MyVisitorImpl() {
//
//        //PAGERANK
//        @Override
//        public boolean visit(int a1, double a2, double a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return false;
//        }
//
//        //CC
//        @Override
//        public boolean visit(int a1, int a2, int a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//
//        //COUNT PATH IN DAG
//        @Override
//        public boolean visit(Object a1, int a2, int a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//
//        //PARTY
//        @Override
//        public boolean visit(int a1) {
//            System.out.println(a1);
//            return true;
//        }
//
//        public boolean visit(int a1,long a2,long a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//    };
}
