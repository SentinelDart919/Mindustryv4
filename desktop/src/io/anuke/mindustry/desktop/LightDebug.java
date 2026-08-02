package io.anuke.mindustry.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import io.anuke.kryonet.KryoClient;
import io.anuke.kryonet.KryoServer;
import io.anuke.mindustry.Mindustry;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.net.Net;
import io.anuke.ucore.util.Log;

import static io.anuke.mindustry.Vars.*;
// FOR DEBUG ONLY
public class LightDebug extends Mindustry{
    private int frames = 0;

    @Override
    public void render(){
        super.render();
        frames++;

        if(frames == 90){
            Log.info("Loading map with darkness");
            logic.reset();
            world.loadMap(world.maps.all().first());
            logic.play();
        }

        if(frames > 90 && frames <= 220){
            state.darkness = 0.6f;
        }

        if(frames == 151){
            Log.info("Dumping during normal gameplay, path: {0}", screenshotDirectory.toString());
            saveScreen(Gdx.files.absolute(screenshotDirectory.toString() + "/screen.png"));
            saveFBO(renderer.lightSurface.getBuffer(), Gdx.files.absolute(screenshotDirectory.toString() + "/lightmap.png"));
            saveFBO(renderer.pixelSurface.getBuffer(), Gdx.files.absolute(screenshotDirectory.toString() + "/pixelsurface.png"));
        }

        if(frames == 220){
            Log.info("Dark screenshot (darkness still 0.6)");
            renderer.takeMapScreenshot();
            state.darkness = 0f;
        }

        if(frames == 240){
            Log.info("Light screenshot (darkness 0)");
            renderer.takeMapScreenshot();
        }

        if(frames == 260){
            Log.info("Dumping screen at darkness 0");
            saveScreen(Gdx.files.absolute(screenshotDirectory.toString() + "/screen_light.png"));
            saveFBO(renderer.pixelSurface.getBuffer(), Gdx.files.absolute(screenshotDirectory.toString() + "/pixelsurface_light.png"));
            Gdx.app.exit();
        }

        if(frames > 400){
            Gdx.app.exit();
        }
    }

    static void saveScreen(FileHandle file){
        int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        for(int i = 0; i < lines.length; i += 4){
            lines[i + 3] = (byte)255;
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        BufferUtils.copy(lines, 0, pixmap.getPixels(), lines.length);
        PixmapIO.writePNG(file, pixmap);
        pixmap.dispose();
        Log.info("Saved screen {0} ({1}x{2})", file.toString(), w, h);
    }

    static void saveFBO(FrameBuffer fbo, FileHandle file){
        fbo.begin();
        int w = fbo.getWidth(), h = fbo.getHeight();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        fbo.end();
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        BufferUtils.copy(lines, 0, pixmap.getPixels(), lines.length);
        PixmapIO.writePNG(file, pixmap);
        pixmap.dispose();
        Log.info("Saved {0} ({1}x{2})", file.toString(), w, h);
    }

    public static void main(String[] arg){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LightDebug");
        config.setWindowedMode(960, 540);
        config.setWindowIcon("sprites/icon.png");
        config.setIdleFPS(120);

        Platform.instance = new DesktopPlatform(arg);

        Net.setClientProvider(new KryoClient());
        Net.setServerProvider(new KryoServer());
        new Lwjgl3Application(new LightDebug(), config);
    }
}
