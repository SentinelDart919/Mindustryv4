package arc.math.geom;

public class Vector3{
    public float x, y, z;
    public Vector3(){}
    public Vector3(float x, float y, float z){ this.x = x; this.y = y; this.z = z; }
    public Vector3 set(Vector3 other){ x = other.x; y = other.y; z = other.z; return this; }
    public Vector3 set(float x, float y, float z){ this.x = x; this.y = y; this.z = z; return this; }
    public Vector3 add(float x, float y, float z){ this.x += x; this.y += y; this.z += z; return this; }
    public Vector3 scl(float s){ x *= s; y *= s; z *= s; return this; }
    public float len(){ return (float)Math.sqrt(x*x + y*y + z*z); }
    public Vector3 nor(){ float l = len(); if(l != 0){ x /= l; y /= l; z /= l; } return this; }
    public Vector3 cpy(){ return new Vector3(x, y, z); }
    public Vector3 sub(Vector3 other){ x -= other.x; y -= other.y; z -= other.z; return this; }
    public static final Vector3 Z = new Vector3(0, 0, 1);
    public static final Vector3 Y = new Vector3(0, 1, 0);
    public static final Vector3 X = new Vector3(1, 0, 0);
}
