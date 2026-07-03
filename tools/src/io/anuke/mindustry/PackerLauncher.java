package io.anuke.mindustry;

import arc.util.Time;
import arc.util.Log;

import java.io.IOException;

public class PackerLauncher {

    public static void main(String[] args) throws IOException {
        Vars.headless = true;
        ImageContext context = new ImageContext();
        context.load();
        Timers.mark();
        Generators.generate(context);
        Log.info("&ly[Generator]&lc Total time to generate: &lg{0}&lcms", Timers.elapsed());
        Log.info("&ly[Generator]&lc Total images created: &lg{0}", Image.total());
        Image.dispose();
    }

}
