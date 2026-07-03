package arc.scene;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
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

public class Skin{
    public ObjectMap<String, Object> resources = new ObjectMap<>();
    
    public Skin(){
    }
    
    public Skin(Table table){
    }

    public Skin(arc.graphics.g2d.TextureAtlas atlas){
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
        return (T)resources.get(name);
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
    }

    public arc.scene.style.Drawable getDrawable(String name){
        return get(name, arc.scene.style.Drawable.class);
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
        return new TextureRegion();
    }
    
    public Color getColor(String name){
        return Color.white;
    }
    
    public ButtonStyle getButtonStyle(){
        return new ButtonStyle();
    }
    
    public ImageButtonStyle getImageButtonStyle(){
        return new ImageButtonStyle();
    }
    
    public LabelStyle getLabelStyle(){
        return new LabelStyle();
    }
    
    public TextButtonStyle getTextButtonStyle(){
        return new TextButtonStyle();
    }
    
    public TextFieldStyle getTextFieldStyle(){
        return new TextFieldStyle();
    }
    
    public CheckBoxStyle getCheckBoxStyle(){
        return new CheckBoxStyle();
    }
    
    public SliderStyle getSliderStyle(){
        return new SliderStyle();
    }
    
    public ScrollPaneStyle getScrollPaneStyle(){
        return new ScrollPaneStyle();
    }
    
    public ProgressBarStyle getProgressBarStyle(){
        return new ProgressBarStyle();
    }
    
    public TouchpadStyle getTouchpadStyle(){
        return new TouchpadStyle();
    }
    
    public DialogStyle getDialogStyle(){
        return new DialogStyle();
    }
}
