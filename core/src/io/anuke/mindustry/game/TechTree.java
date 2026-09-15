package io.anuke.mindustry.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**Represents a named tech tree (tech set) that a selection of content can be assigned to.
 * Content with no tech tree assigned (null/empty) belongs to the default tree.
 * New trees are created with {@link #create(String)} / {@link #create(String, String)} instead of being
 * hardcoded here, so custom content can add its own trees without modifying this class.*/
public class TechTree{
    /**The default tech tree. Holds all content not explicitly assigned to another tree.*/
    public static final String defaultTech = "default";

    private static final Array<String> treeNames = new Array<>();
    private static final ObjectMap<String, String> localizedNames = new ObjectMap<>();

    static{
        //pre-register the default tree
        treeNames.add(defaultTech);
        localizedNames.put(defaultTech, "Default");
    }

    private TechTree(){}

    /**Creates (registers if needed) a new tech tree with the given name, using the name itself as display name.
     * Returns the tree name so it can be assigned to content directly. Safe to call multiple times.*/
    public static String create(String name){
        return create(name, name);
    }

    /**Creates (registers if needed) a new tech tree with the given name and localized display name.
     * Returns the tree name so it can be assigned to content directly. Safe to call multiple times.*/
    public static String create(String name, String localizedName){
        register(name);
        localizedNames.put(name, localizedName);
        return name;
    }

    /**Registers a new tech tree by name. Does nothing if already registered.
     * Shouldn't be needed in most cases; use {@link #create(String)} instead.*/
    public static void register(String name){
        if(!treeNames.contains(name, false)){
            treeNames.add(name);
        }
    }

    /**Returns all registered tree names, with the default tree first.*/
    public static Array<String> all(){
        return treeNames;
    }

    /**Returns whether the given tree name is registered.*/
    public static boolean contains(String name){
        return treeNames.contains(name, false);
    }

    public static String localizedName(String tree){
        if(tree == null || tree.isEmpty()) tree = defaultTech;
        return localizedNames.get(tree, tree);
    }
}