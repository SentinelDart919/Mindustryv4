package arc.graphics.g3d.attributes;

import arc.graphics.Color;

public class ColorAttribute{
    public static final long Diffuse = 1;
    public Color color = Color.white;
    public ColorAttribute(){
    }
    public ColorAttribute(long type, Color color){
        this.color = color;
    }
}
