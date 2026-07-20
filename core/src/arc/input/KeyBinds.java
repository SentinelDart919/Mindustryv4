package arc.input;

import arc.scene.ui.DeviceType;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;

public class KeyBinds{
    public static class Entry{
        public KeyCode key;
        public String name;
        public arc.input.KeyBind.KeybindValue value;
        public Entry(KeyCode key, String name){ this.key = key; this.name = name; }
    }

    public static class Category{
        public String name;
        public Category(String name){ this.name = name; }
    }

    public static class Section{
        public DeviceType device;
        public String name;
        public ObjectMap<String, Entry> entries = new ObjectMap<>();
        public Section(DeviceType device, String name){ this.device = device; this.name = name; }
    }

    private static final ObjectMap<String, Seq<Section>> sections = new ObjectMap<>();
    private static final ObjectMap<String, String> aliases = new ObjectMap<>();

    public static void defaultSection(String name, DeviceType device, Object... args){
        Section sec = new Section(device, name);
        String currentCategory = null;
        for(int i = 0; i < args.length;){
            if(args[i] instanceof Category){
                currentCategory = ((Category)args[i]).name;
                i++;
            }else if(i + 1 < args.length && args[i] instanceof String && args[i + 1] instanceof arc.input.KeyBind.KeybindValue){
                String entryName = (String)args[i];
                arc.input.KeyBind.KeybindValue value = (arc.input.KeyBind.KeybindValue)args[i + 1];
                KeyCode key = null;
                if(value instanceof KeyCode){
                    key = (KeyCode)value;
                }else if(value instanceof arc.input.KeyBind.Axis){
                    key = ((arc.input.KeyBind.Axis)value).key != null ? ((arc.input.KeyBind.Axis)value).key :
                          ((arc.input.KeyBind.Axis)value).min;
                }
                if(key != null){
                    Entry entry = new Entry(key, entryName);
                    entry.value = value;
                    sec.entries.put(entryName, entry);
                }
                i += 2;
            }else{
                i++;
            }
        }
        Seq<Section> seq = sections.get(name);
        if(seq == null){
            seq = new Seq<>();
            sections.put(name, seq);
        }
        seq.add(sec);
    }

    public static void setSectionAlias(String alias, String section){
        aliases.put(alias, section);
    }

    public static @Nullable Section getSection(String name){
        String resolved = aliases.get(name, name);
        Seq<Section> seq = sections.get(resolved);
        if(seq == null || seq.isEmpty()) return null;
        return seq.first();
    }

    public static @Nullable Entry get(String section, String name){
        Section sec = getSection(section);
        return sec != null ? sec.entries.get(name) : null;
    }

    public static @Nullable Entry get(Section section, String name){
        return section != null ? section.entries.get(name) : null;
    }

    public static void load(){
    }
}
