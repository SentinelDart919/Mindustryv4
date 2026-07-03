package arc.util.io;

import java.io.ByteArrayOutputStream;

public class CountableByteArrayOutputStream extends ByteArrayOutputStream{
    public int count;

    public CountableByteArrayOutputStream(){
        super();
    }

    public CountableByteArrayOutputStream(int size){
        super(size);
    }

    @Override
    public void write(int b){
        super.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len){
        super.write(b, off, len);
        count += len;
    }

    public int getCount(){
        return count;
    }

    public void resetCount(){
        count = 0;
    }

    public byte[] getBytes(){
        return buf;
    }
}
