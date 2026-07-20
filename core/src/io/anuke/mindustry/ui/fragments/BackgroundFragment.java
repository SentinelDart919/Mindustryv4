package io.anuke.mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Gfx;
import arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.graphics.Shaders;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.scene.Group;
import arc.scene.ui.layout.Scl;

import static io.anuke.mindustry.Vars.state;

public class BackgroundFragment extends Fragment{

    @Override
    public void build(Group parent){

        Core.scene.table().rect((a, b, w, h) -> {
            Draw.colorl(0.1f);
            Fill.crect(0, 0, w, h);
            Draw.color(Palette.accent);
            Draw.shader(Shaders.menu);
            Fill.crect(0, 0, w, h);
            Draw.shader();
            Draw.color();

            boolean portrait = Core.Gfx.getWidth() < Core.Gfx.getHeight();
            float logoscl = (int) Scl.scl(7) * (portrait ? 5f / 7f : 1f);
            TextureRegion logo = Core.scene.getSkin().getRegion("logotext");
            float logow = logo.width * logoscl;
            float logoh = logo.height * logoscl;

            Draw.color();
            Draw.rect(logo, (int)(w / 2 - logow / 2), (int)(h - logoh + 15 - Scl.scl(portrait ? 30f : 0)), logow, logoh);
        }).visible(() -> state.is(State.menu)).grow();
    }
}

