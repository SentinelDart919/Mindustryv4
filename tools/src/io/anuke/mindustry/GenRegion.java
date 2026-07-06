package io.anuke.mindustry;

import arc.graphics.g2d.TextureAtlas;

import java.awt.image.BufferedImage;

public class GenRegion extends TextureAtlas.AtlasRegion {
    public String name;
    public boolean invalid;
    public ImageContext context;
    public BufferedImage source;
    public int x, y;

    public GenRegion set(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.packedWidth = width;
        this.packedHeight = height;
        this.originalWidth = width;
        this.originalHeight = height;
        return this;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY(){
        return y;
    }

    public static void validate(arc.graphics.g2d.TextureRegion region){
        if(region instanceof GenRegion gen && gen.invalid){
            gen.context.err("Region does not exist: {0}", gen.name);
        }
    }
}
