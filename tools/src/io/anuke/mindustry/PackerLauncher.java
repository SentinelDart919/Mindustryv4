package io.anuke.mindustry;

import arc.util.Time;
import arc.util.Log;
import arc.util.Timers;

import java.io.IOException;

public class PackerLauncher {

    public static void main(String[] args) throws IOException {
        Vars.headless = true;
        ImageContext context = new ImageContext();
        context.load();
        try{
            Timers.mark();
            Generators.generate(context);
            context.packOutput();
            Log.info("&ly[Generator]&lc Total time to generate: &lg" + Timers.elapsed() + "&lcms");
            Log.info("&ly[Generator]&lc Total images created: &lg" + Image.total());
        }finally{
            Image.dispose();
        }
    }

}
