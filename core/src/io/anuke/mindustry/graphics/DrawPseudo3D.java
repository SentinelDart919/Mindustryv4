package io.anuke.mindustry.graphics;

import io.anuke.ucore.util.Mathf;

import static io.anuke.ucore.core.Core.camera;

/** Utility for rendering things at a fake "height" above the ground, making them shift relative to the camera.
 *  Ported from the MineDusty mod (which took it from Meep's 3d pseudo code for the tantros-test-java repo). */
public class DrawPseudo3D{
    public static float xHeight(float x, float height){
        if(height <= 0) return x;
        return x + xOffset(x, height);
    }

    public static float yHeight(float y, float height){
        if(height <= 0) return y;
        return y + yOffset(y, height);
    }

    public static float xOffset(float x, float height){
        return (x - camera.position.x) * hMul(height);
    }

    public static float yOffset(float y, float height){
        return (y - camera.position.y) * hMul(height);
    }

    public static float hScale(float height){
        return 1f + hMul(height);
    }

    public static float hMul(float height){
        return height * scale();
    }

    /** The current display scale, approximated from the camera zoom. */
    public static float scale(){
        return Mathf.clamp(camera.zoom, 0.5f, 4f);
    }

    public static float layerOffset(float x, float y){
        float max = Math.max(camera.viewportWidth, camera.viewportHeight);
        float dx = x - camera.position.x, dy = y - camera.position.y;
        return -Mathf.sqrt(dx * dx + dy * dy) / max / 1000f;
    }

    public static float heightFade(float height){
        float scl = hScale(height);
        return 1f - Mathf.curve(scl, 1.5f, 7f);
    }
}
