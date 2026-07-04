package io.anuke.mindustry.ui;

import arc.Core;
import arc.graphics.g2d.Batch;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.g2d.Draw;
import arc.scene.Element;

public class GridImage extends Element{
    private int imageWidth, imageHeight;

    public GridImage(int w, int h){
        this.imageWidth = w;
        this.imageHeight = h;
    }

    public void draw(Batch batch, float alpha){
        TextureRegion blank = Core.atlas.find("white");

        float xspace = (width / imageWidth);
        float yspace = (height / imageHeight);
        float s = 1f;

        int minspace = 10;

        int jumpx = (int) (Math.max(minspace, xspace) / xspace);
        int jumpy = (int) (Math.max(minspace, yspace) / yspace);

        for(int x = 0; x <= imageWidth; x += jumpx){
            Draw.rect(blank, x + xspace * x - s, y - s, 2f, height + (x == imageWidth ? 1 : 0));
        }

        for(int y = 0; y <= imageHeight; y += jumpy){
            Draw.rect(blank, x - s, y + y * yspace - s, width, 2f);
        }
    }

    public void setImageSize(int w, int h){
        this.imageWidth = w;
        this.imageHeight = h;
    }
}
