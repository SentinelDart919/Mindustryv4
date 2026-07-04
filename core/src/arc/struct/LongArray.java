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
    public boolean remove(long value){
        for(int i = 0; i < size; i++){
            if(items[i] == value){
                removeIndex(i);
                return true;
            }
        }
        return false;
    }
    public long removeIndex(int index){
        if(index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
        long old = items[index];
        size--;
        System.arraycopy(items, index + 1, items, index, size - index);
        return old;
    }
    public void set(int index, long value){ items[index] = value; }
    public void insert(int index, long value){
        if(index > size) throw new IndexOutOfBoundsException(String.valueOf(index));
        if(size >= items.length){
            long[] n = new long[items.length*2];
            System.arraycopy(items,0,n,0,items.length);
            items=n;
        }
        System.arraycopy(items, index, items, index + 1, size - index);
        items[index] = value;
        size++;
    }
    public void truncate(int newSize){
        if(size > newSize) size = newSize;
    }
    public void ensureCapacity(int cap){
        if(items.length < cap){
            long[] n = new long[cap];
            System.arraycopy(items, 0, n, 0, size);
            items = n;
        }
    }
}
