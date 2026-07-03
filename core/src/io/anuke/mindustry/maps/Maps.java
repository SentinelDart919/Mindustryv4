package io.anuke.mindustry.maps;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.struct.Seq;
import arc.util.Disposable;
import arc.struct.ObjectMap;
import io.anuke.mindustry.io.MapIO;
import arc.func.Prov;
import arc.util.Log;
import arc.struct.Seq;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.anuke.mindustry.Vars.*;

public class Maps implements Disposable{
    /**List of all built-in maps.*/
    private static final String[] defaultMapNames = {"sandbox", "infectionmap"};
    /**Tile format version.*/
    private static final int version = 0;

    /**Maps map names to the real maps.*/
    private ObjectMap<String, Map> maps = new ObjectMap<>();
    /**All maps stored in an ordered array.*/
    private Seq<Map> allMaps = new Seq<>();
    /**Temporary array used for returning things.*/
    private Seq<Map> returnArray = new Seq<>();

    /**Returns a list of all maps, including custom ones.*/
    public Seq<Map> all(){
        return allMaps;
    }

    /**Returns a list of only custom maps.*/
    public Seq<Map> customMaps(){
        returnArray.clear();
        for(Map map : allMaps){
            if(map.custom) returnArray.add(map);
        }
        return returnArray;
    }

    /**Returns a list of only default maps.*/
    public Seq<Map> defaultMaps(){
        returnArray.clear();
        for(Map map : allMaps){
            if(!map.custom) returnArray.add(map);
        }
        return returnArray;
    }

    /**Returns map by internal name.*/
    public Map getByName(String name){
        return maps.get(name.toLowerCase());
    }

    /**Load all maps. Should be called at application start.*/
    public void load(){
        try {
            for (String name : defaultMapNames) {
                Fi file = Core.files.internal("maps/" + name + "." + mapExtension);
                loadMap(file.nameWithoutExtension(), file::read, false);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        loadCustomMaps();
    }

    /**Save a map. This updates all values and stored data necessary.*/
    public void saveMap(String name, MapTileData data, ObjectMap<String, String> tags){
        try{
            //create copy of tags to prevent mutation later
            ObjectMap<String, String> newTags = new ObjectMap<>();
            newTags.putAll(tags);
            tags = newTags;

            Fi file = customMapDirectory.child(name + "." + mapExtension);
            MapIO.writeMap(file.write(false), tags, data);

            if(maps.containsKey(name)){
                if(maps.get(name).texture != null) {
                    maps.get(name).texture.dispose();
                    maps.get(name).texture = null;
                }
                allMaps.remove(maps.get(name), true);
            }

            Map map = new Map(name, new MapMeta(version, tags, data.width(), data.height(), null), true, getStreamFor(name));
            if(!headless){
                map.texture = new Texture(MapIO.generatePixmap(data));
            }
            allMaps.add(map);

            maps.put(name, map);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**Removes a map completely.*/
    public void removeMap(Map map){
        if(map.texture != null){
            map.texture.dispose();
            map.texture = null;
        }

        maps.remove(map.name);
        allMaps.remove(map, true);

        customMapDirectory.child(map.name + "." + mapExtension).delete();
    }

    private void loadMap(String name, Prov<InputStream> Prov, boolean custom) throws IOException{
        try(DataInputStream ds = new DataInputStream(Prov.get())) {
            MapMeta meta = MapIO.readMapMeta(ds);
            Map map = new Map(name, meta, custom, Prov);

            if (!headless){
                map.texture = new Texture(MapIO.generatePixmap(MapIO.readTileData(ds, meta, true)));
            }

            maps.put(map.name.toLowerCase(), map);
            allMaps.add(map);
        }
    }

    private void loadCustomMaps(){
        for(Fi file : customMapDirectory.list()){
            try{
                if(file.extension().equalsIgnoreCase(mapExtension)){
                    loadMap(file.nameWithoutExtension(), file::read, true);
                }
            }catch (Exception e){
                Log.err("Failed to load custom map file '{0}'!", file);
                Log.err(e);
            }
        }
    }

    /**Returns an input stream Prov for a given map name.*/
    private Prov<InputStream> getStreamFor(String name){
        return customMapDirectory.child(name + "." + mapExtension)::read;
    }

    @Override
    public void dispose() {

    }
}
