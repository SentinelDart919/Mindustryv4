package io.anuke.mindustry.ui;

import arc.graphics.Texture;
import arc.graphics.g2d.Batch;
import arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.graphics.Palette;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;

public class BorderImage extends Image{
    private float thickness = 3f;

    public BorderImage(){

    }

    public BorderImage(Texture texture){
        super(texture);
    }

    public BorderImage(Texture texture, float thick){
        super(texture);
        thickness = thick;
    }

    public BorderImage(TextureRegion region, float thick){
        super(region);
        thickness = thick;
    }

    @Override
    public void draw(Batch batch, float alpha){
        super.draw(batch, alpha);

        float scaleX = getScaleX();
        float scaleY = getScaleY();

        Draw.color(Palette.accent);
        Lines.stroke(Scl.scl(thickness));
        Lines.rect(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
        Draw.reset();
    }
}
