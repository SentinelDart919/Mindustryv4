package arc.input;

public class KeyBinds{
    public static class KeyBind{
        public KeyCode key;
        public KeyBind(KeyCode key, String name){ this.key = key; }
    }
    public static class Category{
        public String name;
        public Category(String name){ this.name = name; }
    }
    public static void load(){
    }
}
