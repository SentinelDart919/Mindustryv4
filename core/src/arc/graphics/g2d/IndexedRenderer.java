package arc.graphics.g2d;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.math.Mat;

public class IndexedRenderer{
    private final Mat transformMatrix = new Mat();
    private final Mat projectionMatrix = new Mat();

    public IndexedRenderer(int maxSprites){
    }

    public Mat getTransformMatrix(){
        return transformMatrix;
    }

    public void setProjectionMatrix(Mat projection){
        projectionMatrix.set(projection);
    }

    public void render(Texture texture){
    }

    public void draw(int index, TextureRegion region, float x, float y, float width, float height){
    }

    public void draw(int index, TextureRegion region, float x, float y, float width, float height, float rotation){
    }

    public void setColor(Color color){
    }

    public void dispose(){
    }
}
