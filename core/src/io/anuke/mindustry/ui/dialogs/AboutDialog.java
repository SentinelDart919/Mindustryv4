package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.struct.ObjectSet;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.io.Contributors;
import io.anuke.mindustry.io.Contributors.Contributor;
import io.anuke.mindustry.ui.Links;
import io.anuke.mindustry.ui.Links.LinkEntry;
import arc.Core;
import arc.util.Time;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.utils.UIUtils;
import arc.util.OS;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.ios;
import static io.anuke.mindustry.Vars.ui;

public class AboutDialog extends FloatingDialog{
    //private Seq<Contributor> contributors = new Seq<>();
    private static ObjectSet<String> bannedItems = ObjectSet.with("google-play", "itch.io", "dev-builds", "trello");

    public AboutDialog(){
        super("$text.about.button");


        shown(this::setup);
        onResize(this::setup);
    }

    void setup(){
        content().clear();
        buttons().clear();

        float h = UIUtils.portrait() ? 90f : 80f;
        float w = UIUtils.portrait() ? 330f : 600f;

        Table in = new Table();
        ScrollPane pane = new ScrollPane(in);

        for(LinkEntry link : Links.getLinks()){
            if((ios || OS.isMac) && bannedItems.contains(link.name)){ //because Apple doesn't like me mentioning things
                continue;
            }

            Table table = new Table();
            table.margin(0);
            table.table(img -> {
                img.addImage("white").height(h - 5).width(40f).color(link.color);
                img.row();
                img.addImage("white").height(5).width(40f).color(link.color.cpy().mul(0.8f, 0.8f, 0.8f, 1f));
            }).expandY();

            table.table(i -> {
                i.background("button-edge-3");
                i.addImage("icon-" + link.name).size(14 * 3f);
            }).size(h - 5, h);

            table.table(inset -> {
                inset.add("[accent]" + Strings.capitalize(link.name.replace("-", " "))).growX().left();
                inset.row();
                inset.labelWrap(link.description).width(w - 100f).color(Color.lightGray).growX();
            }).padLeft(8);

            table.addImageButton("icon-link", 14 * 3, () -> {
                if(!Core.net.openURI(link.link)){
                    ui.showError("$text.linkfail");
                    Core.app.getClipboard().setContents(link.link);
                }
            }).size(h - 5, h);

            in.add(table).size(w, h).padTop(5).row();
        }

        shown(() -> Timers.run(1f, () -> Core.scene.setScrollFocus(pane)));

        content().add(pane).growX();

        addCloseButton();

        buttons().addButton("$text.credits", this::showCredits).size(200f, 64f);

        if(!ios && !OS.isMac){
            buttons().addButton("$text.changelog.title", ui.changelog::show).size(200f, 64f);
        }

        if(UIUtils.portrait()){
            for(Cell<?> cell : buttons().getCells()){
                cell.width(140f);
            }
        }

    }

    public void showCredits(){
        FloatingDialog dialog = new FloatingDialog("$text.credits");
        dialog.addCloseButton();
        dialog.content().add("$text.credits.text");
        dialog.content().row();

        dialog.show();
    }
}
