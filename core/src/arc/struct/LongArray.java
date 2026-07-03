package arc.struct;

public class LongArray{
    public long[] items = new long[16];
    public int size;
    public LongArray(){}
    public LongArray(int cap){ items = new long[cap]; }
    public void add(long v){
        if(size >= items.length){ long[] n = new long[items.length*2]; System.arraycopy(items,0,n,0,items.length); items=n; }
        items[size++] = v;
    }
    public long get(int i){ return items[i]; }
    public int size(){ return size; }
    public void clear(){ size = 0; }
    public boolean isEmpty(){ return size == 0; }
}