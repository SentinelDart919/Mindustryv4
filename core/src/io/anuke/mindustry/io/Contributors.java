package io.anuke.mindustry.io;

import arc.struct.Seq;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import io.anuke.mindustry.net.Net;
import arc.func.Cons;

import static io.anuke.mindustry.Vars.contributorsURL;

public class Contributors{

    public static void getContributors(Cons<Seq<Contributor>> success, Cons<Throwable> fail){
        Net.http(contributorsURL, "GET", result -> {
            JsonReader reader = new JsonReader();
            JsonValue value = reader.parse(result).child;
            Seq<Contributor> out = new Seq<>();

            while(value != null){
                String login = value.getString("login");
                out.add(new Contributor(login));
                value = value.next;
            }

            success.get(out);
        }, fail);
    }

    public static class Contributor{
        public final String login;

        public Contributor(String login){
            this.login = login;
        }

        @Override
        public String toString(){
            return "Contributor{" +
                    "login='" + login + '\'' +
                    '}';
        }
    }
}
