package arc.math.geom;

import arc.graphics.Camera;

public class PerspectiveCamera extends Camera{
    public PerspectiveCamera(float fov, float viewportWidth, float viewportHeight){
        this.width = viewportWidth;
        this.height = viewportHeight;
    }
    public float fieldOfView = 55f;
}
