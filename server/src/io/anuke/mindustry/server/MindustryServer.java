package io.anuke.mindustry.server;

import arc.modules.ModuleCore;
import arc.modules.Module;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.Logic;
import io.anuke.mindustry.core.NetServer;
import io.anuke.mindustry.core.World;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.io.BundleLoader;
import arc.ApplicationListener;

import static io.anuke.mindustry.Vars.*;

public class MindustryServer extends ModuleCore {
    private String[] args;

    public MindustryServer(String[] args){
        this.args = args;
    }

    @Override
    public void initModules(){
        Vars.init();

        headless = true;

        BundleLoader.load();
        content.verbose(false);
        content.load();
        content.initialize(Content::init);

        module(logic = new Logic());
        module(world = new World());
        module(netServer = new NetServer());
        module((Module)new ServerControl(args));
    }

    @Override
    public void preInit() {

    }
}
