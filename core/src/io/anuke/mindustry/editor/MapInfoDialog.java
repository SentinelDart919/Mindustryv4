package io.anuke.mindustry.editor;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.struct.ObjectMap;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.ui.dialogs.FloatingDialog;
import arc.scene.ui.CheckBox;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextField;

public class MapInfoDialog extends FloatingDialog{
    private final MapEditor editor;

    private TextArea description;
    private TextField author;
    private TextField name;

    public MapInfoDialog(MapEditor editor){
        super("$text.editor.mapinfo");
        this.editor = editor;

        addCloseButton();

        shown(this::setup);

        hidden(() -> {

        });
    }

    private void setup(){
        cont.clear();

        ObjectMap<String, String> tags = editor.getTags();

        cont.add("$text.editor.name").padRight(8).left();

        cont.defaults().padTop(15);

        name = cont.addField(tags.get("name", ""), text -> {
            tags.put("name", text);
        }).size(400, 55f).get();
        name.setMessageText("$text.unknown");

        cont.row();

        cont.add("$text.editor.description").padRight(8).left();

        description = cont.area(tags.get("description", ""), text -> {
            tags.put("description", text);
        }).size(400f, 140f).get();

        cont.row();

        cont.add("$text.editor.author").padRight(8).left();

        author = cont.addField(tags.get("author", Core.settings.getString("mapAuthor", "")),text -> {
            tags.put("author", text);
            Core.settings.put("mapAuthor", text);
            Core.settings.save();
        }).size(400, 55f).get();
        author.setMessageText("$text.unknown");

        cont.row();

        cont.add().padRight(8).left();
        CheckBox check = new CheckBox("$text.editor.oregen");
        check.setChecked(!tags.get("oregen", "0").equals("0"));
        check.changed(() -> tags.put("oregen", check.isChecked() ? "1" : "0"));
        cont.add(check).left();

        name.change();
        description.change();
        author.change();

        Platform.instance.addDialog(name, 50);
        Platform.instance.addDialog(author, 50);
        Platform.instance.addDialog(description, 1000);
    }
}
