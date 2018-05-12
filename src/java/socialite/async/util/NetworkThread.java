package socialite.async.util;

import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import mpi.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class SendRequest {
    private ByteBuffer buffer;
    private int dest;
    private int tag;


    SendRequest(ByteBuffer buffer, int dest, int tag) {
        this.buffer = buffer;
        this.dest = dest;
        this.tag = tag;
    }


    public int getTag() {
        return tag;
    }

    public int getDest() {
        return dest;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

class RecvRequest {
    private ByteBuffer buffer;
    private int source;
    private int tag;

    RecvRequest(ByteBuffer buffer, int source, int tag) {
        this.buffer = buffer;
        this.source = source;
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public int getSource() {
        return source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

public class NetworkThread extends Thread {
    private static final Log L = LogFactory.getLog(NetworkThread.class);
    private final ConcurrentLinkedQueue<SendRequest> sendQueue = new ConcurrentLinkedQueue<>();
    private final List<Request> activeSends = new LinkedList<>();
    private final List<RecvRequest> recvList = new LinkedList<>();
    private volatile boolean shutdown;
    private static NetworkThread instance;

    private NetworkThread() {
        L.info("Network thread created");
    }

    public synchronized static NetworkThread get() {
        if (instance == null) {
            instance = new NetworkThread();
        }
        return instance;
    }

    @Override
    public void run() {
        L.info("network thread started");
        try {
            loop();
        } catch (MPIException e) {
            e.printStackTrace();
        }
    }

    void loop() throws MPIException {
        while (!shutdown) {
            Status status = MPI.COMM_WORLD.iProbe(MPI.ANY_SOURCE, MPI.ANY_TAG);
            if (status != null) {
                int source = status.getSource();
                int tag = status.getTag();
                int sizeInBytes = status.getCount(MPI.BYTE);

                ByteBuffer buffer = MPI.newByteBuffer(sizeInBytes);
                MPI.COMM_WORLD.recv(buffer, sizeInBytes, MPI.BYTE, source, tag);
                RecvRequest recvRequest = new RecvRequest(buffer, source, tag);

                synchronized (recvList) {
                    recvList.add(recvRequest);
                }
            }

            SendRequest sendRequest;
            while ((sendRequest = sendQueue.poll()) != null) {
                ByteBuffer buffer = sendRequest.getBuffer();
                Request request = MPI.COMM_WORLD.iSend(buffer, buffer.position(), MPI.BYTE, sendRequest.getDest(),sendRequest.getTag());
                synchronized (activeSends) {
                    activeSends.add(request);
                }
            }
            //delete sent record
            synchronized (activeSends) {
                Iterator<Request> iterator = activeSends.iterator();
                while (iterator.hasNext()) {
                    Request request = iterator.next();
                    if (request.test()) {
                        request.free();
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void send(byte[] data, int dest, int tag) {
        if (shutdown)
            throw new RuntimeException("The network thread already shutdown");
        ByteBuffer buffer = MPI.newByteBuffer(data.length);
        buffer.put(data);
        SendRequest sendRequest = new SendRequest(buffer, dest, tag);
        sendQueue.add(sendRequest);
    }

    public void send(ByteBuffer buffer, int dest, int tag) {
        if (shutdown)
            throw new RuntimeException("The network thread already shutdown");
        SendRequest sendRequest = new SendRequest(buffer, dest, tag);
        sendQueue.add(sendRequest);
    }


    public byte[] read(int source, int tag) {
        byte[] data;
        while ((data = tryRead(source, tag)) == null) {
            if (shutdown)
                throw new RuntimeException("The network thread already shutdown");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public byte[] tryRead(int source, int tag) {
        byte[] data = null;
        synchronized (recvList) {
            Iterator<RecvRequest> iterator = recvList.iterator();
            while (iterator.hasNext()) {
                RecvRequest recvRequest = iterator.next();
                if (recvRequest.getSource() == source && recvRequest.getTag() == tag) {
                    iterator.remove();
                    ByteBuffer buffer = recvRequest.getBuffer();
                    data = new byte[buffer.remaining()];
                    buffer.get(data);
                    break;//just get one
                }
            }
        }
        return data;
    }

    public ByteBuffer readByteBuffer(int source, int tag) {
        ByteBuffer buffer;
        while ((buffer = tryReadByteBuffer(source, tag)) == null) {
            if (shutdown)
                throw new RuntimeException("The network thread already shutdown");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    public ByteBuffer tryReadByteBuffer(int source, int tag) {
        ByteBuffer buffer = null;
        synchronized (recvList) {
            Iterator<RecvRequest> iterator = recvList.iterator();
            while (iterator.hasNext()) {
                RecvRequest recvRequest = iterator.next();
                if (recvRequest.getSource() == source && recvRequest.getTag() == tag) {
                    iterator.remove();
                    buffer = recvRequest.getBuffer();
                    break;//just get one
                }
            }
        }
        return buffer;
    }

    public void shutdown() {
        //waiting for all sent
        synchronized (activeSends) {
            while (activeSends.size() > 0)
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        shutdown = true;
    }
}
