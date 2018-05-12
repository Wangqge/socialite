package socialite.async.dist.master;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.engine.DistAsyncEngine;
import socialite.dist.master.MasterNode;

public class AsyncMaster {
    public static final int ID = 0;
    private static final Log L = LogFactory.getLog(AsyncMaster.class);
    DistAsyncEngine distAsyncEngine;

    public AsyncMaster(String program) throws InterruptedException {
        while (!MasterNode.getInstance().allOneLine())//waiting workers online
            Thread.sleep(100);
        L.info("Master ready");
        distAsyncEngine = new DistAsyncEngine(program);
    }

    public void startMaster() {
        distAsyncEngine.run();
    }

}
