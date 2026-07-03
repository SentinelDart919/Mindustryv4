package arc.graphics;

import arc.graphics.gl.FrameBuffer;

/**A wrapper around an off-screen framebuffer for rendering effects.*/
public class Surface extends FrameBuffer{
    public Surface(){
    }

    public Surface(int width, int height){
        super(width, height);
    }

    private float scale = 1f;

    public FrameBuffer getBuffer(){
        return this;
    }

    public Surface setSize(int width, int height){
        if(getWidth() != width || getHeight() != height){
            resize(width, height);
        }
        return this;
    }

    public Surface setSize(int width, int height, boolean centered){
        return setSize(width, height);
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    public float getScale(){
        return scale;
    }

    public int width(){
        return getWidth();
    }

    public int height(){
        return getHeight();
    }
}
