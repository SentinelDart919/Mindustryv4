package arc.graphics;

import arc.math.Mathf;
import arc.util.Tmp;

public class Hue{
    private static final float[] hsv = new float[3];

    public static Color shift(Color color, float hue, float saturation){
        color.toHsv(hsv);
        hsv[0] += hue;
        hsv[1] += saturation;
        return color.fromHsv(hsv);
    }

    public static Color mix(Color a, Color b, float amount){
        return Tmp.c1.set(a).lerp(b, amount);
    }

    public static Color mix(Color a, Color b, float amount, Color target){
        return target.set(a).lerp(b, amount);
    }

    public static Color random(){
        return Tmp.c1.set(Color.white).hue(Mathf.random(360f));
    }
}
