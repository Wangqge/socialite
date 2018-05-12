package socialite.async.codegen;

import socialite.tables.QueryVisitor;
import socialite.visitors.VisitorImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseAsyncTable {


    public abstract int getSize();

    public abstract void iterate(QueryVisitor queryVisitor);

    public boolean updateLockFree(int localInd) {
        throw new NotImplementedException();
    }

    public boolean updateLockFree(int localInd, int iter) {
        throw new NotImplementedException();
    }

    public abstract double getPriority(int localInd);

    public abstract double accumulateValue();

    public abstract double accumulateDelta();

    public abstract VisitorImpl getInitVisitor();

    public abstract VisitorImpl getEdgeVisitor();

    public VisitorImpl getExtraVisitor() {
        throw new NotImplementedException();
    }
}
