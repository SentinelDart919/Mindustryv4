package io.anuke.mindustry.io;

import arc.struct.Seq;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import io.anuke.mindustry.net.Net;
import arc.func.Cons;

import static io.anuke.mindustry.Vars.releasesURL;

public class Changelogs{

    public static void getChangelog(Cons<Seq<VersionInfo>> success, Cons<Throwable> fail){
        Net.http(releasesURL, "GET", result -> {
            JsonReader reader = new JsonReader();
            JsonValue value = reader.parse(result);
            Seq<VersionInfo> out = new Seq<>();

            for(JsonValue entry = value.child; entry != null; entry = entry.next){
                String name = entry.getString("name");
                String description = entry.getString("body").replace("\r", "");
                int id = entry.getInt("id");
                String tagName = entry.getString("tag_name");
                int build = 0;
                try{
                    String buildString = tagName.startsWith("v") ? tagName.substring(1) : tagName;
                    if(buildString.contains(".")){
                        buildString = buildString.substring(0, buildString.indexOf("."));
                    }
                    if(buildString.contains("-")){
                        buildString = buildString.substring(0, buildString.indexOf("-"));
                    }
                    build = Integer.parseInt(buildString);
                }catch(Exception e){
                    //ignore parsing errors
                }
                out.add(new VersionInfo(name, description, id, build, entry.getString("published_at")));
            }

            success.get(out);
        }, fail);
    }

    public static class VersionInfo{
        public final String name, description, date;
        public final int id, build;

        public VersionInfo(String name, String description, int id, int build, String date){
            this.name = name;
            this.description = description;
            this.id = id;
            this.build = build;
            this.date = date;
        }

        @Override
        public String toString(){
            return "VersionInfo{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", id=" + id +
                    ", build=" + build +
                    '}';
        }
    }
}
