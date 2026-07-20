package arc.scene;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.CheckBox.CheckBoxStyle;
import arc.scene.ui.Dialog.DialogStyle;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.ProgressBar.ProgressBarStyle;
import arc.scene.ui.ScrollPane.ScrollPaneStyle;
import arc.scene.ui.Slider.SliderStyle;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.Touchpad.TouchpadStyle;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Reflect;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;

public class Skin{
    public ObjectMap<String, Object> resources = new ObjectMap<>();
    private TextureAtlas atlas;

    public Skin(){
    }

    public Skin(Table table){
    }

    public Skin(TextureAtlas atlas){
        this.atlas = atlas;
    }

    public void add(String name, Object resource){
        resources.put(name, resource);
    }

    public void add(Object resource){
        add(resource.getClass().getSimpleName(), resource);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type){
        return (T)resources.get(type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, Class<T> type){
        Object object = resources.get(name);
        if(object != null && type.isInstance(object)) return (T)object;
        Object prefixed = resources.get(type.getSimpleName() + "#" + name);
        if(prefixed != null) return (T)prefixed;
        return (T)object;
    }

    @SuppressWarnings("unchecked")
    public <T> T optional(Class<T> type){
        return (T)resources.get(type.getSimpleName());
    }

    public boolean has(String name, Class<?> type){
        return resources.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T optional(String name, Class<T> type){
        return (T)resources.get(name);
    }

    public void load(arc.files.Fi file){
        Core.scene.addStyle(Skin.class, this);
        JsonValue root;
        try{
            root = new JsonReader().parse(file);
        }catch(Exception e){
            System.err.println("Skin: Failed to parse skin JSON: " + e.getMessage());
            return;
        }
        if(root == null) return;
        for(JsonValue entry = root.child; entry != null; entry = entry.next){
            String className = entry.name();
            JsonValue styles = entry.child;
            if(styles == null) continue;

            if(className.equals("Color")){
                for(JsonValue colorEntry = styles; colorEntry != null; colorEntry = colorEntry.next){
                    resources.put(colorEntry.name(), parseColor(colorEntry));
                }
            }else if(className.equals("TintedDrawable")){
                for(JsonValue drawEntry = styles; drawEntry != null; drawEntry = drawEntry.next){
                    String baseName = drawEntry.getString("name", "white");
                    Drawable base = resolveDrawable(baseName);
                    if(base instanceof TextureRegionDrawable){
                        JsonValue colorVal = drawEntry.get("color");
                        if(colorVal != null){
                            Color tintColor = parseColor(colorVal);
                            Drawable tinted = ((TextureRegionDrawable)base).tint(tintColor);
                            resources.put(drawEntry.name(), tinted);
                        }
                    }
                }
            }else{
                try{
                    String innerName = className.endsWith("Style") ? className : className + "Style";
                    if(innerName.equals("KeybindDialogStyle")) continue;
                    if("WindowStyle".equals(innerName)){
                        for(JsonValue styleEntry = styles; styleEntry != null; styleEntry = styleEntry.next){
                            String styleName = styleEntry.name();
                            DialogStyle styleObj = new DialogStyle();
                            populateFields(styleObj, styleEntry);
                            resources.put(styleName, styleObj);
                            if("default".equals(styleName)){
                                Core.scene.addStyle(DialogStyle.class, styleObj);
                            }
                        }
                        continue;
                    }
                    String outerName = innerName.replace("Style", "");
                    String fqcn = "arc.scene.ui." + outerName + "$" + innerName;
                    Class<?> styleClass = Class.forName(fqcn);
                    for(JsonValue styleEntry = styles; styleEntry != null; styleEntry = styleEntry.next){
                        String styleName = styleEntry.name();
                        Object existing = resources.get(styleName);
                        Object styleObj = styleClass.getDeclaredConstructor().newInstance();
                        populateFields(styleObj, styleEntry);
                        resources.put(styleName, styleObj);
                        //also store with type prefix to handle cross-section name conflicts
                        resources.put(innerName + "#" + styleName, styleObj);
                        if("default".equals(styleName) || "default-horizontal".equals(styleName) || "default-vertical".equals(styleName)){
                            Core.scene.addStyle((Class)styleClass, styleObj);
                            add(styleObj);
                        }
                    }
                }catch(Exception ignored){
                }
            }
        }
    }

    private void populateFields(Object obj, JsonValue json){
        for(JsonValue field = json.child; field != null; field = field.next){
            String fieldName = field.name();
            if(fieldName == null) continue;
            try{
                java.lang.reflect.Field targetField = getField(obj.getClass(), fieldName);
                if(targetField == null) continue;
                Class<?> fieldType = targetField.getType();
                Object value = resolveValue(field, fieldType);
                if(value != null){
                    targetField.setAccessible(true);
                    targetField.set(obj, value);
                }
            }catch(Exception ignored){
            }
        }
    }

    private java.lang.reflect.Field getField(Class<?> type, String name){
        try{
            return type.getField(name);
        }catch(NoSuchFieldException e){
            if(type.getSuperclass() != null){
                return getField(type.getSuperclass(), name);
            }
            return null;
        }
    }

    private Object resolveValue(JsonValue json, Class<?> expectedType){
        if(json.isString()){
            String str = json.asString();
            if(str == null || str.isEmpty()) return null;
            if(expectedType == Color.class) return resolveColor(str);
            if(expectedType == Drawable.class || Drawable.class.isAssignableFrom(expectedType)){
                return resolveDrawable(str);
            }
            if(expectedType == arc.graphics.g2d.Font.class){
                Object font = resources.get(str);
                if(font instanceof arc.graphics.g2d.Font) return font;
                return null;
            }
            if(expectedType == String.class) return str;
            if(expectedType == Boolean.class || expectedType == boolean.class){
                return str.equals("true");
            }
            if(expectedType == Integer.class || expectedType == int.class){
                try{ return Integer.parseInt(str); }catch(Exception e){ return null; }
            }
            if(expectedType == Float.class || expectedType == float.class){
                try{ return Float.parseFloat(str); }catch(Exception e){ return null; }
            }
            Object resource = resources.get(str);
            if(resource != null && expectedType.isInstance(resource)) return resource;
            return null;
        }
        if(json.isObject()){
            if(expectedType == Color.class){
                return parseColor(json);
            }
            if(expectedType == Drawable.class || Drawable.class.isAssignableFrom(expectedType)){
                String name = json.getString("name", "");
                if(!name.isEmpty()){
                    Drawable base = resolveDrawable(name);
                    JsonValue colorVal = json.get("color");
                    if(colorVal != null && base instanceof TextureRegionDrawable){
                        return ((TextureRegionDrawable)base).tint(parseColor(colorVal));
                    }
                    return base;
                }
            }
            try{
                Object nested = expectedType.getDeclaredConstructor().newInstance();
                populateFields(nested, json);
                return nested;
            }catch(Exception e){
                return null;
            }
        }
        if(json.isNumber()){
            if(expectedType == Float.class || expectedType == float.class) return json.asFloat();
            if(expectedType == Integer.class || expectedType == int.class) return json.asInt();
            if(expectedType == Double.class || expectedType == double.class) return (double)json.asFloat();
            if(expectedType == Long.class || expectedType == long.class) return (long)json.asLong();
            if(expectedType == String.class) return json.asString();
            return null;
        }
        if(json.isBoolean()){
            if(expectedType == Boolean.class || expectedType == boolean.class) return json.asBoolean();
            return null;
        }
        return null;
    }

    private Drawable resolveDrawable(String name){
        Object existing = resources.get(name);
        if(existing instanceof Drawable) return (Drawable)existing;
        if(atlas != null){
            TextureAtlas.AtlasRegion region = atlas.find(name);
            if(region != null && region.found()){
                TextureRegionDrawable drawable = new TextureRegionDrawable(region);
                resources.put(name, drawable);
                return drawable;
            }
        }
        return null;
    }

    private Color resolveColor(String name){
        Object existing = resources.get(name);
        if(existing instanceof Color) return (Color)existing;
        try{
            Color color = Color.valueOf(name);
            resources.put(name, color);
            return color;
        }catch(Exception e){
            return Color.white;
        }
    }

    private Color parseColor(JsonValue json){
        if(json == null) return Color.white.cpy();
        if(json.isString()){
            return Color.valueOf(json.asString());
        }
        if(json.has("hex")){
            return Color.valueOf(json.getString("hex"));
        }
        float r = json.getFloat("r", 1);
        float g = json.getFloat("g", 1);
        float b = json.getFloat("b", 1);
        float a = json.getFloat("a", 1);
        return new Color(r, g, b, a);
    }

    public Drawable getDrawable(String name){
        Object object = resources.get(name);
        if(object instanceof Drawable) return (Drawable)object;
        if(atlas != null){
            TextureAtlas.AtlasRegion region = atlas.find(name);
            if(region != null && region.found()){
                TextureRegionDrawable drawable = new TextureRegionDrawable(region);
                resources.put(name, drawable);
                return drawable;
            }
        }
        return null;
    }

    public <T> ObjectMap<String, T> getAll(Class<T> type){
        ObjectMap<String, T> result = new ObjectMap<>();
        for(ObjectMap.Entry<String, Object> entry : resources.entries()){
            if(type.isInstance(entry.value)){
                result.put(entry.key, (T)entry.value);
            }
        }
        return result;
    }

    public arc.graphics.g2d.Font getFont(String name){
        return get(name, arc.graphics.g2d.Font.class);
    }

    public TextureRegion getRegion(String name){
        if(atlas != null){
            TextureAtlas.AtlasRegion region = atlas.find(name);
            if(region != null) return region;
        }
        return new TextureRegion();
    }

    public Color getColor(String name){
        Color c = get(name, Color.class);
        return c != null ? c : Color.white;
    }

    public ButtonStyle getButtonStyle(){
        return get(ButtonStyle.class);
    }

    public ImageButtonStyle getImageButtonStyle(){
        return get(ImageButtonStyle.class);
    }

    public LabelStyle getLabelStyle(){
        return get(LabelStyle.class);
    }

    public TextButtonStyle getTextButtonStyle(){
        return get(TextButtonStyle.class);
    }

    public TextFieldStyle getTextFieldStyle(){
        return get(TextFieldStyle.class);
    }

    public CheckBoxStyle getCheckBoxStyle(){
        return get(CheckBoxStyle.class);
    }

    public SliderStyle getSliderStyle(){
        return get(SliderStyle.class);
    }

    public ScrollPaneStyle getScrollPaneStyle(){
        return get(ScrollPaneStyle.class);
    }

    public ProgressBarStyle getProgressBarStyle(){
        return get(ProgressBarStyle.class);
    }

    public TouchpadStyle getTouchpadStyle(){
        return get(TouchpadStyle.class);
    }

    public DialogStyle getDialogStyle(){
        return get(DialogStyle.class);
    }
}
