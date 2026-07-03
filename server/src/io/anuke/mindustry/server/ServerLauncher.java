package io.anuke.mindustry.server;

import arc.ApplicationListener;
import arc.Core;
import arc.backends.headless.HeadlessApplication;
import arc.backends.headless.HeadlessApplicationConfiguration;
import io.anuke.kryonet.KryoClient;
import io.anuke.kryonet.KryoServer;
import io.anuke.mindustry.net.Net;
import arc.Settings;

import io.anuke.mindustry.server.CrashHandler;

public class ServerLauncher extends HeadlessApplication{

    public ServerLauncher(ApplicationListener listener, HeadlessApplicationConfiguration config){
        super(listener, config);
    }

    public static void main(String[] args){
        try{

            Net.setClientProvider(new KryoClient());
            Net.setServerProvider(new KryoServer());

            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            Settings.setPrefHandler((appName) -> Core.files.local("config"));

            new ServerLauncher(new MindustryServer(args), config);
        }catch(Throwable t){
            CrashHandler.handle(t);
        }

        //find and handle uncaught exceptions in libGDX thread
        for(Thread thread : Thread.getAllStackTraces().keySet()){
            if(thread.getName().equals("HeadlessApplication")){
                thread.setUncaughtExceptionHandler((t, throwable) -> {
                    CrashHandler.handle(throwable);
                    System.exit(-1);
                });
                break;
            }
        }
    }
}