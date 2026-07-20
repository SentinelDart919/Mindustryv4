package io.anuke.mindustry;

import arc.Core;
import io.anuke.mindustry.core.*;
import io.anuke.mindustry.sounds.Sounds;
import io.anuke.mindustry.game.EventType.GameLoadEvent;
import io.anuke.mindustry.io.BundleLoader;
import arc.Events;
import arc.scene.Scene;
import arc.util.Time;
import arc.util.Timers;
import arc.ApplicationListener;
import arc.util.Log;

import static io.anuke.mindustry.Vars.*;

public class Mindustry extends arc.modules.ModuleCore{

    @Override
    public void initModules(){
        Timers.mark();

        Vars.init();

        Log.setUseColors(false);
        BundleLoader.load();
        content.load();
        schematics.load();

        Core.scene = new Scene();
        Core.input.addProcessor(Core.scene);

        module(logic = new Logic());
        module(world = new World());
        module(soundController = new SoundController());
        Sounds.init();
        module(control = new Control());
        module(renderer = new Renderer());
        module(ui = new UI());
        module(netServer = new NetServer());
        module(netClient = new NetClient());
        module(musicController = new MusicController());
    }

    @Override
    public void preInit() {

    }

    @Override
    public void postInit(){
        launchManager.load();
        Log.info("Time to load [total]: {0}", Timers.elapsed());
        Events.fire(new GameLoadEvent());
    }

    @Override
    public void update(){
        threads.handleBeginRender();
        super.update();
        threads.handleEndRender();
    }

}
