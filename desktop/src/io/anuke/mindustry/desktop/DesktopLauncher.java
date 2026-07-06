package io.anuke.mindustry.desktop;

import arc.ApplicationListener;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.Files;
import io.anuke.kryonet.KryoClient;
import io.anuke.kryonet.KryoServer;
import io.anuke.mindustry.Mindustry;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.net.Net;

public class DesktopLauncher extends SdlApplication{

    public DesktopLauncher(ApplicationListener listener, SdlConfig config){
        super(listener, config);
    }

    public static void main(String[] arg){
        try{
            SdlConfig config = new SdlConfig();
            config.title = "MindustryV4Modded";
            config.maximized = true;
            config.width = 960;
            config.height = 540;
            config.setWindowIcon(Files.FileType.classpath, "sprites/icon.png");

            Platform.instance = new DesktopPlatform(arg);

            Net.setClientProvider(new KryoClient());
            Net.setServerProvider(new KryoServer());
            new DesktopLauncher(new Mindustry(), config);
        }catch(Throwable e){
            CrashHandler.handle(e);
        }
    }
}
