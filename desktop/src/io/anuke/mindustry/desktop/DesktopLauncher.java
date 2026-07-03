package io.anuke.mindustry.desktop;

import arc.ApplicationListener;
import arc.backends.sdl.SdlApplication;
import arc.backends.sdl.SdlApplicationConfiguration;
import io.anuke.kryonet.KryoClient;
import io.anuke.kryonet.KryoServer;
import io.anuke.mindustry.Mindustry;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.net.Net;

public class DesktopLauncher extends SdlApplication{

    public DesktopLauncher(ApplicationListener listener, SdlApplicationConfiguration config){
        super(listener, config);
    }

    public static void main(String[] arg){
        try{
            SdlApplicationConfiguration config = new SdlApplicationConfiguration();
            config.setTitle("MindustryV4Modded");
            config.setMaximized(true);
            config.setWindowedMode(960, 540);
            config.setWindowIcon("sprites/icon.png");

            Platform.instance = new DesktopPlatform(arg);

            Net.setClientProvider(new KryoClient());
            Net.setServerProvider(new KryoServer());
            new DesktopLauncher(new Mindustry(), config);
        }catch(Throwable e){
            CrashHandler.handle(e);
        }
    }
}
