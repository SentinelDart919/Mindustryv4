package io.anuke.mindustry.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.anuke.kryonet.KryoClient;
import io.anuke.kryonet.KryoServer;
import io.anuke.mindustry.Mindustry;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.net.Net;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Log.LogHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DesktopLauncher extends Lwjgl3Application{

    public DesktopLauncher(ApplicationListener listener, Lwjgl3ApplicationConfiguration config){
        super(listener, config);
    }

    public static void main(String[] arg){
        try{
            Log.setLogger(new LogHandler(){
                private final PrintWriter writer = openWriter();

                private PrintWriter openWriter(){
                    try{
                        return new PrintWriter(new BufferedWriter(new FileWriter(new File(DesktopPlatform.getMainDirectory(), "last_log.txt"))), true);
                    }catch(Throwable t){
                        return null;
                    }
                }

                @Override
                public void info(String text, Object... args){
                    print("INFO", text, args);
                }

                @Override
                public void warn(String text, Object... args){
                    print("WARN", text, args);
                }

                @Override
                public void err(String text, Object... args){
                    print("ERROR", text, args);
                }

                @Override
                public void err(Throwable e){
                    if(writer != null){
                        writer.println("[ERROR] " + e.toString());
                        e.printStackTrace(writer);
                        writer.flush();
                    }
                    e.printStackTrace();
                }

                private void print(String level, String text, Object... args){
                    if(writer != null){
                        writer.println("[" + level + "] " + Log.format(text, false, args));
                        writer.flush();
                    }
                    System.out.println(Log.format(text + "&fr", true, args));
                }
            });

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
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
