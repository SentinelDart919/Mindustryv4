package io.anuke.mindustry.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.Shader;
import arc.util.Timers;
import arc.scene.ui.layout.Scl;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;

public class Shaders{
    public static Outline outline;
    public static BlockBuild blockbuild;
    public static BlockPreview blockpreview;
    public static Shield shield;
    public static SurfaceShader water;
    public static SurfaceShader lava;
    public static SurfaceShader oil;
    public static Space space;
    public static UnitBuild build;
    public static MixShader mix;
    public static Shader fullMix;
    public static FogShader fog;
    public static MenuShader menu;

    public static void init(){
        outline = new Outline();
        blockbuild = new BlockBuild();
        blockpreview = new BlockPreview();
        shield = new Shield();
        water = new SurfaceShader("water");
        lava = new SurfaceShader("lava");
        oil = new SurfaceShader("oil");
        space = new Space();
        build = new UnitBuild();
        mix = new MixShader();
        fog = new FogShader();
        fullMix = new Shader(vert("default"), frag("fullmix"));
        menu = new MenuShader();
    }

    private static String vert(String name){
        return Core.files.internal("shaders/" + name + ".vertex").readString();
    }

    private static String frag(String name){
        return Core.files.internal("shaders/" + name + ".fragment").readString();
    }

    public static class MenuShader extends Shader{
        float time = 0f;

        public MenuShader(){
            super(vert("default"), frag("menu"));
        }

        @Override
        public void apply(){
            time = time % 158;

            setUniformf("u_resolution", Core.Gfx.getWidth(), Core.Gfx.getHeight());
            setUniformi("u_time", (int)(time += Core.graphics.getDeltaTime() * 60f));
            setUniformf("u_uv", Draw.getBlankRegion().u, Draw.getBlankRegion().v);
            setUniformf("u_scl", Scl.scl(1f));
            setUniformf("u_uv2", Draw.getBlankRegion().u2, Draw.getBlankRegion().v2);
        }
    }

    public static class FogShader extends Shader{
        public FogShader(){
            super(vert("default"), frag("fog"));
        }
    }

    public static class MixShader extends Shader{
        public Color color = new Color(Color.white);

        public MixShader(){
            super(vert("default"), frag("mix"));
        }

        @Override
        public void apply(){
            super.apply();
            setUniformf("u_color", color);
        }
    }

    public static class Space extends SurfaceShader{

        public Space(){
            super("space2");
        }

        @Override
        public void apply(){
            super.apply();
            setUniformf("u_center", world.width() * tilesize / 2f, world.height() * tilesize / 2f);
        }
    }

    public static class UnitBuild extends Shader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public UnitBuild(){
            super(vert("default"), frag("build"));
        }

        @Override
        public void apply(){
            setUniformf("u_time", time);
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class Outline extends Shader{
        public Color color = new Color();
        public TextureRegion region;

        public Outline(){
            super(vert("default"), frag("outline"));
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class BlockBuild extends Shader{
        public Color color = new Color();
        public float progress;
        public TextureRegion region;

        public BlockBuild(){
            super(vert("default"), frag("blockbuild"));
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_color", color);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_time", Timers.time());
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class BlockPreview extends Shader{
        public Color color = new Color();
        public TextureRegion region;

        public BlockPreview(){
            super(vert("default"), frag("blockpreview"));
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class Shield extends Shader{

        public Shield(){
            super(vert("default"), frag("shield"));
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", Timers.time() / Scl.scl(1f));
            setUniformf("u_offset",
                    Core.camera.position.x - Core.camera.width / 2 * Core.camera.zoom,
                    Core.camera.position.y - Core.camera.height / 2 * Core.camera.zoom);
            setUniformf("u_texsize", Core.camera.width * Core.camera.zoom,
            Core.camera.height * Core.camera.zoom);
        }
    }

    public static class SurfaceShader extends Shader{

        public SurfaceShader(String frag){
            super(vert("default"), Shaders.frag(frag));
        }

        @Override
        public void apply(){
            setUniformf("camerapos",
                    Core.camera.position.x - Core.camera.width / 2 * Core.camera.zoom,
                    Core.camera.position.y - Core.camera.height / 2 * Core.camera.zoom);
            setUniformf("screensize", Core.camera.width* Core.camera.zoom,
            Core.camera.height * Core.camera.zoom);
            setUniformf("time", Timers.time());
        }
    }
}

