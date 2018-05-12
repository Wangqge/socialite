package socialite.async.util;
import socialite.async.atomic.MyAtomicDouble;

import  java.sql.Timestamp;

public class sertest {
    private  MyAtomicDouble s;
    private  int d;
    private int k;
    public sertest(){
        d=2;
        s=new MyAtomicDouble(0.0);
        k=11;
    }
//    public sertest(int num){
//      //  s=0;
//        d=num;
//    }

    public int getD() {
        return d;
    }


    public void setD(int d) {
        this.d = d;
    }
    public double getS() {
        return s.doubleValue();

    }

    public void setS(double s) {
            this.s.getAndSet(s);
    }
}
