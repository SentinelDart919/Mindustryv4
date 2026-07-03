package arc.util;

import java.nio.*;

public class BufferUtils{
    public static void copy(byte[] src, int srcOffset, Buffer dst, int count){
        if(dst instanceof ByteBuffer){
            ((ByteBuffer)dst).put(src, srcOffset, count);
        }
    }
}
