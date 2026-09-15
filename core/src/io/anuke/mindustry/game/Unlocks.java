package io.anuke.mindustry.game;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.EventType.UnlockEvent;
import io.anuke.mindustry.maps.campaign.CampaignRegistry;
import io.anuke.mindustry.type.ContentType;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Settings;

import static io.anuke.mindustry.Vars.state;

/**Stores player unlocks. Clientside only.*/
public class Unlocks{
    private ObjectMap<ContentType, ObjectSet<String>> unlocked = new ObjectMap<>();
    private String campaign = CampaignRegistry.serpulo;
    private boolean dirty;

    static{
        Settings.setSerializer(ContentType.class, (stream, t) -> stream.writeInt(t.ordinal()), stream -> ContentType.values()[stream.readInt()]);
    }

    /**Sets the currently active campaign, saving the previous campaign's unlocks and loading the new one's.*/
    public void setCampaign(String campaign){
        if(campaign == null || campaign.equals(this.campaign)){
            if(campaign != null) this.campaign = campaign;
            return;
        }
        save();
        this.campaign = campaign;
        load();
    }

    public String getCampaign(){
        return campaign;
    }

    private String settingKey(){
        return "unlockset-" + campaign;
    }

    /** Returns whether or not this piece of content is unlocked yet.
     * In infinite resource modes (e.g. sandbox) everything is available during a game, even if it wasn't actually unlocked.*/
    public boolean isUnlocked(UnlockableContent content){
        if(state.mode.infiniteResources && !state.is(State.menu)) return true;

        if(content.alwaysUnlocked()) return true;

        if(!unlocked.containsKey(content.getContentType())){
            unlocked.put(content.getContentType(), new ObjectSet<>());
        }

        ObjectSet<String> set = unlocked.get(content.getContentType());

        return set.contains(content.getContentName());
    }

    /**
     * Makes this piece of content 'unlocked', if possible, using standard game rules.
     * If this piece of content is already unlocked or cannot be unlocked due to dependencies, nothing changes.
     *
     * @return whether or not this content was newly unlocked.
     */
    public boolean unlockContent(UnlockableContent content){
        if(state.mode.infiniteResources) return false;
        return researchContent(content);
    }
    public boolean researchContent(UnlockableContent content){
        if(!content.canBeUnlocked() || content.alwaysUnlocked()) return false;

        if(!unlocked.containsKey(content.getContentType())){
            unlocked.put(content.getContentType(), new ObjectSet<>());
        }

        boolean ret = unlocked.get(content.getContentType()).add(content.getContentName());

        //fire unlock event so other classes can use it
        if(ret){
            content.onUnlock();
            Events.fire(new UnlockEvent(content));
            dirty = true;
        }

        return ret;
    }

    /** Returns whether unlockables have changed since the last save.*/
    public boolean isDirty(){
        return dirty;
    }

    /** Clears all unlocked content. Automatically saves.*/
    public void reset(){
        unlocked.clear();
        dirty = false;
        save();
    }

    public void load(){
        unlocked = Settings.getObject(settingKey(), ObjectMap.class, ObjectMap::new);
    }

    public void save(){
        Settings.putObject(settingKey(), unlocked);
        Settings.save();
    }

}