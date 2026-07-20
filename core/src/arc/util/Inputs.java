package arc.util;

import arc.Core;
import arc.input.InputProcessor;
import arc.input.KeyBind;
import arc.input.KeyBinds;
import arc.input.KeyCode;

public class Inputs{
    public enum DeviceType{
        keyboard, controller
    }

    public static void useControllers(boolean use){
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

    public static boolean keyDown(KeyBinds.Section section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyDown(entry.key);
    }

    public static boolean keyDown(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyDown(entry.key);
    }

    public static boolean keyTap(String name){
        return Core.input.keyTap(KeyCode.valueOf(name));
    }

    public static boolean keyTap(KeyBinds.Section section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyTap(entry.key);
    }

    public static boolean keyTap(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyTap(entry.key);
    }

    public static boolean keyRelease(String name){
        return false;
    }

    public static boolean keyRelease(KeyBinds.Section section, String name){
        return false;
    }

    public static boolean keyRelease(String section, String name){
        return false;
    }

    public static boolean getAxisActive(String name){
        return false;
    }

    public static float getAxisTapped(KeyBinds.Section section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        if(entry != null){
            KeyCode key = resolveAxisKey(entry);
            if(key != null) return Core.input.axis(key);
        }
        return 0f;
    }

    public static float getAxisTapped(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        if(entry != null){
            KeyCode key = resolveAxisKey(entry);
            if(key != null) return Core.input.axis(key);
        }
        return 0f;
    }

    public static float getAxis(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        if(entry != null){
            KeyCode key = resolveAxisKey(entry);
            if(key != null) return Core.input.axis(key);
        }
        return 0f;
    }

    public static float getAxis(String name){
        return 0f;
    }

    public static float scroll(){
        return 0f;
    }

    private static KeyCode resolveAxisKey(KeyBinds.Entry entry){
        if(entry.value instanceof KeyCode){
            return (KeyCode)entry.value;
        }else if(entry.value instanceof KeyBind.Axis){
            KeyBind.Axis axis = (KeyBind.Axis)entry.value;
            return axis.key != null ? axis.key : axis.min;
        }
        return entry.key;
    }
}
