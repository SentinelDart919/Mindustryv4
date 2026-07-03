package arc.math;

public class Random{
    public java.util.Random random = new java.util.Random();
    
    public Random(){
    }
    
    public Random(long seed){
        setSeed(seed);
    }
    
    public void setSeed(long seed){
        random.setSeed(seed);
    }
    
    public int nextInt(){
        return random.nextInt();
    }
    
    public int nextInt(int range){
        return range <= 0 ? 0 : random.nextInt(range);
    }
    
    public long nextLong(){
        return random.nextLong();
    }
    
    public float nextFloat(){
        return random.nextFloat();
    }
    
    public double nextDouble(){
        return random.nextDouble();
    }
    
    public boolean nextBoolean(){
        return random.nextBoolean();
    }
}
