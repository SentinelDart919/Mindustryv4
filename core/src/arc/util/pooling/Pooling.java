package arc.util.pooling;

import arc.func.Prov;
import arc.util.pooling.Pool.Poolable;

public class Pooling{
    public static <T extends Poolable> T obtain(Class<T> type, Prov<T> supplier){
        return Pools.obtain(type, supplier);
    }

    public static void free(Poolable object){
        Pools.free(object);
    }

    public static void free(Object object){
        if(object instanceof Poolable){
            Pools.free((Poolable)object);
        }
    }
}
