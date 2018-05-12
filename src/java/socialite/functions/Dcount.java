package socialite.functions;

public final class Dcount extends AbstractAggregation {
    static Dcount inst = new Dcount();
    public static Dcount get() { return inst; }

    public int apply(int a, int b) { throw new AssertionError("can't call this method directly"); }
    public long apply(long a, long b) { throw new AssertionError("can't call this method directly"); }
    public float apply(float a, float b) { throw new AssertionError("can't call this method directly"); }
    public double apply(double a, double b) { throw new AssertionError("can't call this method directly"); }
    public String apply(String a, String b) { throw new AssertionError("can't call this method directly"); }
}
