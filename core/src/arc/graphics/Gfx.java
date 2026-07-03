package arc.graphics;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.struct.Seq;
import arc.math.geom.Vec2;
import arc.graphics.g2d.Draw;

public class Gfx{
    public static Surface effectSurface;
    public static Surface pixelSurface;

    public static void clear(Color color){
        Core.gl.glClearColor(color.r, color.g, color.b, color.a);
        Core.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public static void surface(Surface surface, boolean clear, boolean alpha){
        if(surface != null){
            surface.begin();
            if(clear) Gfx.clear(Color.clear);
        }else if(effectSurface != null){
            effectSurface.end();
        }
    }

    public static void surface(Surface surface){ surface.begin(); }
    public static void surface(){ if(effectSurface != null) effectSurface.end(); }
    public static void flushSurface(){}
    public static Seq<Surface> getSurfaces(){
        Seq<Surface> seq = new Seq<>();
        if(effectSurface != null) seq.add(effectSurface);
        if(pixelSurface != null) seq.add(pixelSurface);
        return seq;
    }
    public static Surface getEffectSurface(){ return effectSurface; }
    public static void setCameraScale(int scale){}
    public static void beginCam(){}
    public static void end(){ Draw.flush(); }
    public static void beginShaders(Shader shader){}
    public static void endShaders(){}
    public static void shader(Shader shader, boolean apply){}
    public static void shader(Shader shader){}
    public static void shader(){}
    public static void setAdditiveBlending(){}
    public static void setNormalBlending(){}
    public static void beginClip(float x, float y, float w, float h){}
    public static void endClip(){}
    public static Vec2 mouseWorld(){
        Vec2 v = new Vec2();
        Core.camera.unproject(v.set(Core.input.mouseX(), Core.input.mouseY()));
        return v;
    }
    public static void begin(){}
    public static void useBatch(){}
    public static void popBatch(){}
    public static int getWidth(){ return Core.graphics.getWidth(); }
    public static int getHeight(){ return Core.graphics.getHeight(); }
    public static Surface createSurface(int scale){
        return new Surface(Core.graphics.getWidth() / scale, Core.graphics.getHeight() / scale);
    }
    public static Surface createSurface(){
        return new Surface(Core.graphics.getWidth(), Core.graphics.getHeight());
    }
}

