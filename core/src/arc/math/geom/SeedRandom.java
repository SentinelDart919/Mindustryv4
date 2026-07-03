package arc.math.geom;

import arc.math.Mathf;

public class SeedRandom{
    public long seed;
    
    public SeedRandom(){}
    
    public SeedRandom(long seed){
        this.seed = seed;
    }
    
    public SeedRandom(long seed, int mod){
        this.seed = seed;
    }
    
    public boolean chance(double chance){
        return nextFloat() < chance;
    }

    public float nextFloat(){
        return Mathf.random(1f);
    }
    
    public int nextInt(){
        return Mathf.random(1000000);
    }
    
    public int nextInt(int range){
        return Mathf.random(range);
    }
    
    public long nextLong(){
        return (long)(Mathf.random(1000000));
    }
}
