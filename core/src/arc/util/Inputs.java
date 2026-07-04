package arc.util;

import arc.Core;
import arc.input.InputProcessor;
import arc.input.KeyCode;

public class Inputs{
    public static void useControllers(boolean use){
        //TODO
    }

    public static void addProcessor(InputProcessor processor){
        Core.input.addProcessor(processor);
    }

    public static void addProcessor(int priority, InputProcessor processor){
        Core.input.addProcessor(processor);
    }

    public static boolean keyDown(String name){
        return Core.input.keyDown(KeyCode.valueOf(name));
    }

    public static boolean keyDown(KeyCode key){
        return Core.input.keyDown(key);
    }

    public static boolean keyTap(String name){
        return Core.input.keyTap(KeyCode.valueOf(name));
    }

    public static float getAxis(String section, String name){
        return Core.input.axis(KeyCode.valueOf(name));
    }

    public static float getAxis(String name){
        return 0f;
    }

    public static float scroll(){
        return 0f;
    }
}
