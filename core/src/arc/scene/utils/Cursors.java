package arc.scene.utils;

import arc.Core;
import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.Cursor;
import arc.graphics.Pixmap;
import arc.struct.ObjectMap;

public class Cursors{
    public static Cursor arrow;
    public static Cursor ibeam;
    public static Cursor hand;

    public static int cursorScaling = 1;
    public static Color outlineColor = Color.black;

    private static ObjectMap<String, Cursor> customCursors = new ObjectMap<>();

    public static Cursor loadCursor(String name){
        //Implementation depends on backend, but for compat we just return a placeholder or null
        return null; 
    }

    public static void setHand(){
        if(hand != null) Core.graphics.setCursor((Graphics.Cursor)hand);
    }

    public static void set(String name){
        if(customCursors.containsKey(name)){
            Core.graphics.setCursor((Graphics.Cursor) customCursors.get(name));
        }
    }

    public static void restoreCursor(){
        if(arrow != null) Core.graphics.setCursor((Graphics.Cursor) arrow);
    }

    public static void loadCustom(String name){
        customCursors.put(name, loadCursor(name));
    }
}