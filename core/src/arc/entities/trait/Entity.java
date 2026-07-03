package arc.entities.trait;

import arc.entities.Entities;
import arc.entities.EntityGroup;

public interface Entity extends PosTrait, MoveTrait{
    int getID();
    void resetID(int id);

    default void update(){}
    default void removed(){}
    default void added(){}

    default EntityGroup targetGroup(){
        return Entities.defaultGroup();
    }

    default void add(){
        targetGroup().add(this);
    }

    default void remove(){
        if(getGroup() != null){
            getGroup().remove(this);
        }
        setGroup(null);
    }

    EntityGroup getGroup();
    void setGroup(EntityGroup group);

    default boolean isAdded(){
        return getGroup() != null;
    }
}