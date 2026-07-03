package arc.util;

import arc.graphics.Color;

public class Hue{
    public static Color mix(Color a, Color b, float progress, Color target){
        return target.set(a).lerp(b, progress);
    }
}
