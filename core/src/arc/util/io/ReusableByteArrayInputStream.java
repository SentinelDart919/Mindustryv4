package arc.util.io;

import java.io.ByteArrayInputStream;

public class ReusableByteArrayInputStream extends ByteArrayInputStream{

    public ReusableByteArrayInputStream(){
        super(new byte[]{});
    }

    public ReusableByteArrayInputStream(byte[] buf){
        super(buf);
    }

    public void setBytes(byte[] buf){
        this.buf = buf;
        this.count = buf.length;
        this.pos = 0;
        this.mark = 0;
    }

    public void setBytes(byte[] buf, int offset, int length){
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }
}
