package io.anuke.mindustry.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.ucore.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.control;

/**Base interface for an unlockable content type.*/
public abstract class UnlockableContent extends MappableContent{
    /**The tech trees this content belongs to.*/
    public ObjectSet<String> techTree = new ObjectSet<>();

    /**Assigns this content to exactly the given tech tree. Returns this content for chaining.
     * Null or empty assigns it to the default tree.*/
    public UnlockableContent setTechTree(String techTree){
        this.techTree.clear();
        if(techTree != null && !techTree.isEmpty()){
            this.techTree.add(techTree);
        }
        return this;
    }

    /**Assigns this content to exactly the given tech trees (the set is replaced). Returns this content for chaining.
     * Pass {@link TechTree#defaultTech} to also make it part of the default tree.*/
    public UnlockableContent setTechTrees(String... techTrees){
        this.techTree.clear();
        if(techTrees != null){
            for(String tree : techTrees){
                if(tree != null && !tree.isEmpty()){
                    this.techTree.add(tree);
                }
            }
        }
        return this;
    }

    /**
     * Adds this content to the given tech tree, keeping any existing trees. Returns this content for chaining.
     * Used to make content available in several tech trees
     */
    public void addTechTree(String techTree){
        if(techTree != null && !techTree.isEmpty()){
            this.techTree.add(techTree);
        }
    }

    /**Returns whether this content belongs to the given tech tree.
     * An empty tree set means the default tree only. Content explicitly assigned to trees belongs to those
     * trees; include {@link TechTree#defaultTech} in the set to keep it in the default tree as well.*/
    public boolean belongsToTech(String tech){
        if(tech == null || tech.equals(TechTree.defaultTech)){
            return techTree.size == 0 || techTree.contains(TechTree.defaultTech);
        }
        return techTree.contains(tech);
    }

    /**Returns the localized name of this content.*/
    public abstract String localizedName();

    public abstract TextureRegion getContentIcon();

    /**This should show all necessary info about this content in the specified table.*/
    public abstract void displayInfo(Table table);

    /**Called when this content is unlocked. Use this to unlock other related content.*/
    public void onUnlock(){
    }

    /**Whether this content is always hidden in the content info dialog.*/
    public boolean isHidden(){
        return false;
    }

    /**Override to make content always unlocked.*/
    public boolean alwaysUnlocked(){
        return false;
    }

    /**Lists the content that must be unlocked in order for this specific content to become unlocked. May return null.*/
    public UnlockableContent[] getDependencies(){
        return null;
    }

    /**Returns whether dependencies are satisfied for unlocking this content.*/
    public boolean canBeUnlocked(){
        UnlockableContent[] depend = getDependencies();
        if(depend == null){
            return true;
        }else{
            for(UnlockableContent cont : depend){
                if(!control.unlocks.isUnlocked(cont)){
                    return false;
                }
            }
            return true;
        }
    }
}
