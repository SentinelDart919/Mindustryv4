package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.struct.Seq;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.io.Changelogs;
import io.anuke.mindustry.io.Changelogs.VersionInfo;
import io.anuke.mindustry.game.Version;
import arc.Settings;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.OS;

import static io.anuke.mindustry.Vars.ios;

public class ChangelogDialog extends FloatingDialog{
    private final float vw = 600;
    private Seq<VersionInfo> versions;

    public ChangelogDialog(){
        super("$text.changelog.title");

        addCloseButton();

        content().add("$text.changelog.loading");

        if(!ios && !OS.isMac){
            Changelogs.getChangelog(result -> {
                versions = result;
                Core.app.postRunnable(this::setup);
            }, t -> {
                Log.err(t);
                Core.app.postRunnable(this::setup);
            });
        }
    }

    void setup(){
        Table table = new Table();
        ScrollPane pane = new ScrollPane(table);

        content().clear();
        content().add(pane).grow();

        if(versions == null){
            table.add("$text.changelog.error");
            if(Vars.android){
                table.row();
                table.add("$text.changelog.error.android").padTop(8);
            }

            if(ios){
                table.row();
                table.add("$text.changelog.error.ios").padTop(8);
            }
        }else{
            for(VersionInfo info : versions){
                String desc = info.description;

                desc = desc.replace("Android", "Mobile");

                Table in = new Table();
                in.top().left().margin(10);

                in.add("[accent]" + info.name + "[LIGHT_GRAY]  | " + info.date);
                if(info.build == Version.build){
                    in.row();
                    in.add("$text.changelog.current");
                }else if(info == versions.first()){
                    in.row();
                    in.add("$text.changelog.latest");
                }
                in.row();
                in.labelWrap("[lightgray]" + desc).width(vw - 20).padTop(12);

                table.add(in).width(vw).pad(8).row();
            }

            int lastid = Settings.getInt("lastBuild");
            if(lastid != 0 && versions.peek().build > lastid){
                Settings.putInt("lastBuild", versions.peek().build);
                Settings.save();
                show();
            }
        }
    }
}
