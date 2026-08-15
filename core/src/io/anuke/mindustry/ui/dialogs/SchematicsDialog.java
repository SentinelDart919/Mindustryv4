package io.anuke.mindustry.ui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import io.anuke.mindustry.game.Schematic;
import io.anuke.mindustry.input.PlaceMode;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.ucore.scene.ui.Image;
import io.anuke.ucore.scene.ui.Label;
import io.anuke.ucore.scene.ui.ScrollPane;
import io.anuke.ucore.scene.ui.TextButton;
import io.anuke.ucore.scene.ui.TextField;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Bundles;

import static io.anuke.mindustry.Vars.*;

public class SchematicsDialog extends FloatingDialog {
    private String search = "";

    public SchematicsDialog() {
        super("$text.schematics");
        addBackButton(this);
        shown(this::rebuild);
    }

    private void addBackButton(FloatingDialog dialog) {
        float w = Math.min(230f, Math.max(120f, Gdx.graphics.getWidth() * 0.35f));
        dialog.buttons().addImageTextButton("$text.back", "icon-arrow-left", 30f, dialog::hide).size(w, 64f);
        dialog.keyDown(key -> {
            if (key == Keys.ESCAPE || key == Keys.BACK) {
                Gdx.app.postRunnable(dialog::hide);
            }
        });
    }

    void rebuild() {
        content().clear();

        Table top = new Table();
        TextField searchField = new TextField(search);
        searchField.setMessageText(Bundles.get("text.schematic.search"));
        searchField.setTextFieldListener((field, c) -> {
            search = field.getText();
            rebuild();
        });
        top.add(searchField).growX().height(40).pad(4);
        content().add(top).growX().row();

        Table table = new Table();
        table.margin(10);

        String lower = search.toLowerCase();
        int cols = Math.max(1, Math.min(4, (int) (Gdx.graphics.getWidth() / 235f)));
        int i = 0;
        for (Schematic s : schematics.all()) {
            if (!search.isEmpty() && !s.name().toLowerCase().contains(lower)) continue;
            table.add(card(s)).pad(6).top();
            if (++i % cols == 0) table.row();
        }

        if (i == 0) {
            table.add(new Label(Bundles.get("text.schematic.search"))).pad(20);
        }

        ScrollPane pane = new ScrollPane(table);
        content().add(pane).grow();
    }

    private Table card(Schematic s) {
        Table card = new Table("button");
        card.margin(4);

        TextButton place = new TextButton("", "clear");
        place.clicked(() -> place(s));
        place.add(new SchematicImage(s)).size(190, 118).pad(2);

        card.add(place).size(198, 126).row();

        Label name = new Label(s.name());
        name.setEllipsis(true);
        card.add(name).growX().left().padTop(4).row();

        Table actions = new Table();
        actions.addImageButton("icon-info", "clear", 24, () -> info(s)).size(32, 28).pad(2);
        actions.addImageButton("icon-trash", "clear", 24, () -> delete(s)).size(32, 28).pad(2);
        card.add(actions).row();

        return card;
    }

    private void place(Schematic s) {
        control.input(0).schematic = s.copy();
        control.input(0).mode = PlaceMode.schematic;
        hide();
    }

    private void delete(Schematic s) {
        ui.showConfirm("$text.schematic.delete.title", Bundles.format("text.schematic.delete.text", s.name()), () -> {
            schematics.remove(s);
            rebuild();
        });
    }

    private void info(Schematic s) {
        FloatingDialog dialog = new FloatingDialog(s.name());

        float maxItemWidth = Math.min(500f, Gdx.graphics.getWidth() * 0.5f);
        float previewSize = Math.max(220f, Math.min(480f, Math.min(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.4f)));

        float aspect = s.width / (float) Math.max(1, s.height);
        float pw = previewSize, ph = previewSize / aspect;
        if (ph > previewSize) {
            ph = previewSize;
            pw = ph * aspect;
        }

        dialog.content().add(new SchematicImage(s)).size(pw, ph).padTop(8).row();

        if (!s.description().isEmpty()) {
            Label desc = new Label(s.description());
            desc.setWrap(true);
            desc.setAlignment(Align.center);
            dialog.content().add(desc).width(Math.min(560f, Gdx.graphics.getWidth() * 0.85f)).padTop(6).row();
        }

        Label size = new Label(Bundles.format("text.schematic.size", s.width, s.height));
        size.setAlignment(Align.center);
        dialog.content().add(size).growX().padTop(8).row();

        Label blocks = new Label(Bundles.format("text.schematic.blocks", s.tiles.size));
        blocks.setAlignment(Align.center);
        dialog.content().add(blocks).growX().row();

        Array<ItemStack> reqs = s.requirements();
        if (reqs.size > 0) {
            Label reqTitle = new Label(Bundles.get("text.schematic.requirements"));
            reqTitle.setAlignment(Align.center);
            dialog.content().add(reqTitle).growX().padTop(8).row();

            int itemsPerRow = Math.max(1, Math.min(3, (int) (maxItemWidth / 150f)));
            Table req = new Table();
            Table row = new Table();
            int count = 0;
            for (ItemStack stack : reqs) {
                row.table(chip -> {
                    chip.addImage(stack.item.region).size(8 * 2);
                    chip.add(stack.item.localizedName()).color(Color.LIGHT_GRAY).padLeft(2);
                    chip.add(stack.amount + "").padLeft(5);
                }).pad(3);
                if (++count % itemsPerRow == 0) {
                    req.add(row).pad(2);
                    req.row();
                    row = new Table();
                }
            }
            if (count % itemsPerRow != 0) {
                req.add(row).pad(2);
                req.row();
            }
            dialog.content().add(req).padBottom(6);
        }

        addBackButton(dialog);
        dialog.show();
    }

    class SchematicImage extends Image {
        private final Schematic schem;
        private boolean requested;

        SchematicImage(Schematic schem) {
            this.schem = schem;
            setScaling(Scaling.fit);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (!requested) {
                requested = true;
                Gdx.app.postRunnable(() -> {
                    Texture texture = schematics.getPreview(schem);
                    if (texture != null && getParent() != null) {
                        TextureRegion region = new TextureRegion(texture);
                        region.flip(false, true);
                        setDrawable(region);
                        setScaling(Scaling.fit);
                    }
                });
            }
        }
    }
}
