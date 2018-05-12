package socialite.async.util;

import java.io.IOException;
import java.sql.Timestamp;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.Delegate;
import socialite.async.atomic.MyAtomicDouble;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * protostuff timestamp Delegate
 * @author jiujie
 * @version $Id: TimestampDelegate.java, v 0.1 2016年7月20日 下午2:08:11 jiujie Exp $
 */
public class AtomicDelegate implements Delegate<MyAtomicDouble> {

    public FieldType getFieldType() {
        //return FieldType.FIXED64;
        return FieldType.DOUBLE;
    }

    public Class<?> typeClass() {
        return MyAtomicDouble.class;
    }

    public MyAtomicDouble readFrom(Input input) throws IOException {
        return new MyAtomicDouble(input.readDouble());
    }

    public void writeTo(Output output, int number, MyAtomicDouble value,
                        boolean repeated) throws IOException {
        output.writeDouble(number, value.doubleValue(), repeated);
    }

    public void transfer(Pipe pipe, Input input, Output output, int number,
                         boolean repeated) throws IOException {
        output.writeDouble(number, input.readDouble(), repeated);
    }

}