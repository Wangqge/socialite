package socialite.async.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.dist.worker.WorkerNode;
import socialite.parser.Table;
import socialite.resource.DistTablePartitionMap;
import socialite.resource.SRuntimeWorker;
import socialite.resource.TableInstRegistry;
import socialite.tables.TableInst;
import socialite.visitors.VisitorImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForWorker {
    public static final Log L = LogFactory.getLog(ForWorker.class);

    public static void main(String[] args) throws InterruptedException {
        WorkerNode.startWorkerNode();

//        new Thread(() -> {
//            AsyncWorker worker = null;
//            try {
//                worker = new AsyncWorker();
//                worker.startWorker();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public static Runnable runnable = () -> {
        System.out.println("call test");
        TableInstRegistry tableInstRegistry = SRuntimeWorker.getInst().getTableRegistry();
        Table table = SRuntimeWorker.getInst().getTableMap().get("Edge");
        TableInst[] tableInsts = tableInstRegistry.getTableInstArray(table.id());

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(String.format("/home/gengl/edge_result_%d.txt", SRuntimeWorker.getInst().getWorkerAddrMap().myIndex())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        VisitorImpl visitor = new VisitorForSave(writer);
        DistTablePartitionMap partitionMap = SRuntimeWorker.getInst().getPartitionMap();
        int partitionNum = partitionMap.partitionNum(table.id());
        L.info(String.format("partition num:%d", partitionNum));
        for (TableInst tableInst : tableInsts) {
            Method method = null;
            try {
                method = tableInst.getClass().getDeclaredMethod("iterate", VisitorImpl.class);
                Field baseField = tableInst.getClass().getDeclaredField("base");
                baseField.setAccessible(true);
                int base = (Integer) baseField.get(tableInst);
                System.out.println(String.format("myIdx:%d base:%d", SRuntimeWorker.getInst().getWorkerAddrMap().myIndex(), base));
                if (!tableInst.isEmpty()) {
                    method.invoke(tableInst, visitor);
                    tableInst.clear();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return;
//        SRuntimeWorker runtimeWorker = SRuntimeWorker.getInst();
//        Map<String, Table> tableMap = runtimeWorker.getTableMap();
//        DistTablePartitionMap partitionMap = runtimeWorker.getPartitionMap();
//        TableInstRegistry tableInstRegistry = runtimeWorker.getTableRegistry();
//        Table edge = tableMap.get("edge");
//        TableInst[] tableInsts = tableInstRegistry.getTableInstArray(edge.id());
//        for (TableInst tableInst : tableInsts) {
//            if (!tableInst.isEmpty()) {
//                Class<TableInst> tableInstClass = (Class<TableInst>) tableInst.getClass();
//                try {
//                    Method method = tableInstClass.getMethod("iterate", VisitorImpl.class);
//                    try {
//                        method.invoke(tableInst, new VisitorImpl() {
//                            boolean called;
//                            @Override
//                            public boolean visit_0(int a1) {
//                                if(!partitionMap.isLocal(edge.id(),a1)){
//                                    L.error("error partition");
//                                }
////                                if(!called) {
////                                    L.info(String.format("Worker %d first %d", runtimeWorker.getWorkerAddrMap().myIndex(), a1));
////                                    called = true;
////                                }
//                                return false;
//                            }
//
//                            @Override
//                            public boolean visit(int a1) {
//                                return false;
//                            }
//                        });
//
//                    } catch (IllegalAccessException | InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    };

    static class VisitorForSave extends VisitorImpl {
        BufferedWriter writer;

        VisitorForSave(BufferedWriter writer) {
            this.writer = writer;
        }

        int src;

        @Override
        public boolean visit_0(int a1) {
            src = a1;
            return true;
        }

        @Override
        public boolean visit(int a1) {
            try {
                writer.write(String.format("%d\t%d\n", src, a1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

}
