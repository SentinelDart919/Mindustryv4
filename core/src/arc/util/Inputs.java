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
        return keyDown("default", name);
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
        return keyTap("default", name);
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
        return keyRelease("default", name);
    }

    public static boolean keyRelease(KeyBinds.Section section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyRelease(entry.key);
    }

    public static boolean keyRelease(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        return entry != null && Core.input.keyRelease(entry.key);
    }

    public static boolean getAxisActive(String name){
        return getAxisActive("default", name);
    }

    public static boolean getAxisActive(String section, String name){
        KeyBinds.Entry entry = KeyBinds.get(section, name);
        if(entry != null && entry.value instanceof arc.input.KeyBind.Axis){
            arc.input.KeyBind.Axis axis = (arc.input.KeyBind.Axis)entry.value;
            if(axis.key != null){
                return Core.input.axis(axis.key) != 0;
            }else{
                return Core.input.axis(axis.min) != 0 || Core.input.axis(axis.max) != 0;
            }
        }
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
        return getAxis("default", name);
    }

    public static float scroll(){
        return 0f;
    }

    private static KeyCode resolveAxisKey(KeyBinds.Entry entry){
        if(entry.value instanceof KeyCode){
            return (KeyCode)entry.value;
        }else if(entry.value instanceof KeyBind.Axis){
            KeyBind.Axis axis = (KeyBind.Axis)entry.value;
            if(axis.key != null) return axis.key;
            if(Core.input.keyDown(axis.max)) return axis.max;
            if(Core.input.keyDown(axis.min)) return axis.min;
            return axis.min;
        }
        return entry.key;
    }
}
