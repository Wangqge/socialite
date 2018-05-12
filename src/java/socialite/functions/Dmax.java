package socialite.functions;

public class Dmax extends AbstractAggregation {
    static Dmax inst = new Dmax();
    public static Dmax get() { return inst; }

    public int apply(int a, int b) { throw new AssertionError("can't call this method directly"); }
    public long apply(long a, long b) { throw new AssertionError("can't call this method directly"); }
    public float apply(float a, float b) { throw new AssertionError("can't call this method directly"); }
    public double apply(double a, double b) { throw new AssertionError("can't call this method directly"); }
}
