package arc.math.geom;

public class Vector3{
    public float x, y, z;
    public Vector3(){}
    public Vector3(float x, float y, float z){ this.x = x; this.y = y; this.z = z; }
    public Vector3(Vector3 other){ this.x = other.x; this.y = other.y; this.z = other.z; }
    public Vector3 set(Vector3 other){ x = other.x; y = other.y; z = other.z; return this; }
    public Vector3 set(float x, float y, float z){ this.x = x; this.y = y; this.z = z; return this; }
    public Vector3 set(float[] values){ this.x = values[0]; this.y = values[1]; this.z = values[2]; return this; }
    public Vector3 add(Vector3 other){ x += other.x; y += other.y; z += other.z; return this; }
    public Vector3 add(float x, float y, float z){ this.x += x; this.y += y; this.z += z; return this; }
    public Vector3 sub(Vector3 other){ x -= other.x; y -= other.y; z -= other.z; return this; }
    public Vector3 sub(float x, float y, float z){ this.x -= x; this.y -= y; this.z -= z; return this; }
    public Vector3 scl(float s){ x *= s; y *= s; z *= s; return this; }
    public Vector3 scl(Vector3 other){ x *= other.x; y *= other.y; z *= other.z; return this; }
    public float len(){ return (float)Math.sqrt(x*x + y*y + z*z); }
    public float len2(){ return x*x + y*y + z*z; }
    public Vector3 nor(){ float l = len(); if(l != 0){ x /= l; y /= l; z /= l; } return this; }
    public float dot(Vector3 other){ return x * other.x + y * other.y + z * other.z; }
    public Vector3 crs(Vector3 other){
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    public Vector3 mulAdd(Vector3 v, float s){
        x += v.x * s; y += v.y * s; z += v.z * s;
        return this;
    }
    public Vector3 mulAdd(Vector3 v, Vector3 w){
        x += v.x * w.x; y += v.y * w.y; z += v.z * w.z;
        return this;
    }
    public Vector3 cpy(){ return new Vector3(x, y, z); }
    public Vector3 setZero(){ x = 0; y = 0; z = 0; return this; }
    public boolean isZero(){ return x == 0 && y == 0 && z == 0; }
    public static final Vector3 Z = new Vector3(0, 0, 1);
    public static final Vector3 Y = new Vector3(0, 1, 0);
    public static final Vector3 X = new Vector3(1, 0, 0);
}
