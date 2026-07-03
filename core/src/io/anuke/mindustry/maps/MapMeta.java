package io.anuke.mindustry.maps;

import arc.struct.IntIntMap;
import arc.struct.ObjectMap;
import arc.util.Bundles;
import arc.util.Strings;

public class MapMeta{
    public final int version;
    public final ObjectMap<String, String> tags;
    public final int width, height;
    public final IntIntMap blockMap;

    public MapMeta(int version, ObjectMap<String, String> tags, int width, int height, IntIntMap blockMap){
        this.version = version;
        this.tags = tags;
        this.width = width;
        this.height = height;
        this.blockMap = blockMap;
    }

    public String author(){
        return tag("author");
    }

    public String description(){
        return tag("description");
    }

    public String name(){
        return tag("name");
    }

    public String tag(String name){
        return tags.containsKey(name) && !tags.get(name).trim().isEmpty() ? tags.get(name): Bundles.get("text.unknown");
    }

    public boolean hasOreGen(){
        return !tags.get("oregen", "0").equals("0");
    }

    @Override
    public String toString(){
        return "MapMeta{" +
                "tags=" + tags +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
