package io.anuke.mindustry.graphics;

import arc.graphics.Color;
import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.Shader;

import static io.anuke.mindustry.Vars.renderer;

public enum CacheLayer{
    water{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.water);
        }
    },
    lava{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.lava);
        }
    },
    oil{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.oil);
        }
    },
    space{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.space);
        }
    },
    normal;

    public void begin(){

    }

    public void end(){

    }

    protected void beginShader(){
        //renderer.getBlocks().endFloor();
        renderer.effectSurface.getBuffer().begin();
        Gfx.clear(Color.clear);
        //renderer.getBlocks().beginFloor();
    }

    public void endShader(Shader shader){
        renderer.blocks.endFloor();

        //renderer.effectSurface.getBuffer().end();

        renderer.pixelSurface.getBuffer().begin();

        Gfx.shader(shader);
        Gfx.begin();
        Draw.rect(renderer.effectSurface.texture(), Core.camera.position.x, Core.camera.position.y,
                Core.camera.width * Core.camera.zoom, -Core.camera.height * Core.camera.zoom);
        Gfx.end();
        Gfx.shader();
        renderer.blocks.beginFloor();
    }
}

