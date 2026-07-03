package arc.util.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface TypeSerializer<T>{
    void write(DataOutput stream, T object) throws IOException;
    T read(DataInput stream) throws IOException;
}
