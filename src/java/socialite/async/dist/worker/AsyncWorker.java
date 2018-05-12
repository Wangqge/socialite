package socialite.async.dist.worker;

import mpi.MPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.AsyncConfig;
import socialite.async.util.TextUtils;
import socialite.resource.SRuntimeWorker;
import socialite.tables.QueryVisitor;
import socialite.tables.Tuple;
import socialite.yarn.ClusterConf;

public class AsyncWorker {
    private static final Log L = LogFactory.getLog(AsyncWorker.class);
    private DistAsyncRuntime distAsyncRuntime;

    public AsyncWorker() throws InterruptedException {
        distAsyncRuntime = DistAsyncRuntime.getInst();
    }

    public void startWorker() {
        while (SRuntimeWorker.getInst() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int myWorkerId = ClusterConf.get().getRank() - 1;

        L.info(String.format("worker %d online", myWorkerId));
        distAsyncRuntime.run();
        L.info("worker " + myWorkerId + " saving...");

        AsyncConfig asyncConfig = AsyncConfig.get();

        TextUtils textUtils = null;
        String savePath = asyncConfig.getSavePath();
        if (savePath.length() > 0)
            textUtils = new TextUtils(asyncConfig.getSavePath(), "part-" + myWorkerId);
        if (textUtils != null || asyncConfig.isPrintResult()) {
            TextUtils finalTextUtils = textUtils;
            distAsyncRuntime.getAsyncTable().iterate(new QueryVisitor() {
                @Override
                public boolean visit(Tuple _0) {
                    if (asyncConfig.isPrintResult())
                        L.info(_0.toString());
                    if (finalTextUtils != null)
                        finalTextUtils.writeLine(_0.toString());
                    return true;
                }

                @Override
                public void finish() {
                    if (finalTextUtils != null)
                        finalTextUtils.close();
                }
            });//save result
        }
    }
}
