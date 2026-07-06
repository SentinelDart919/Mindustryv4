package arc.input;

import arc.scene.ui.DeviceType;
import arc.struct.Seq;
import arc.util.Nullable;

public class KeyBinds{
    public static class Entry{
        public KeyCode key;
        public arc.input.KeyBind.KeybindValue value;
        public Entry(KeyCode key, String name){ this.key = key; }
    }

    public static class Category{
        public String name;
        public Category(String name){ this.name = name; }
    }

    public static class Section{
        public DeviceType device;
        public Section(DeviceType device){ this.device = device; }
    }

    private static final Seq<Section> sections = new Seq<>();

    public static void defaultSection(String name, DeviceType device, Object... args){
        sections.add(new Section(device));
    }

    public static void setSectionAlias(String alias, String section){
    }

    public static @Nullable Section getSection(String name){
        return sections.isEmpty() ? null : sections.first();
    }

    public static @Nullable Entry get(String section, String name){
        return null;
    }

    public static @Nullable Entry get(Section section, String name){
        return null;
    }

    public static void load(){
    }
}
