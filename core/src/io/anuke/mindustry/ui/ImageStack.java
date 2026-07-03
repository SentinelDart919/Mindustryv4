package io.anuke.mindustry.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;

public class ImageStack extends Stack{

    public ImageStack(TextureRegion... regions){
        for(TextureRegion region : regions){
            add(new Image(region));
        }
    }
}
