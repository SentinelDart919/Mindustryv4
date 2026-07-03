package io.anuke.mindustry.maps.missions;

import arc.util.Bundles;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.threads;

/**A mission which simply runs a single action and is completed instantly.*/
public class ActionMission extends Mission{
    protected Runnable runner;

    public ActionMission(Runnable runner){
        this.runner = runner;
    }

    public ActionMission(){
    }

    @Override
    public void onComplete(){
        threads.run(runner);
    }

    @Override
    public boolean isComplete(){
        return true;
    }

    @Override
    public String displayString(){
        return Bundles.get("text.loading");
    }
}
