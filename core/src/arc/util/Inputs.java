package arc.util;

import arc.Core;
import arc.input.KeyCode;

public class Inputs{
    public static void useControllers(boolean use){
        //TODO
    }

    public static void addProcessor(arc.input.InputProcessor processor){
        arc.Core.input.addProcessor(processor);
    }

    public static boolean keyDown(String name){
        return Core.input.keyDown(KeyCode.valueOf(name));
    }

    public static boolean keyTap(String name){
        return Core.input.keyTap(KeyCode.valueOf(name));
    }

    public static float getAxis(String section, String name){
        return Core.input.axis(KeyCode.valueOf(name));
    }
}
