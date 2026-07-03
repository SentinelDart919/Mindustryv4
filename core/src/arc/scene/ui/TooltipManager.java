package arc.scene.ui;

import arc.scene.Element;

public class TooltipManager{
    public static TooltipManager instance = new TooltipManager();

    public static TooltipManager getInstance(){
        return instance;
    }

    public float initialTime = 1.5f;
    public float subsequentTime = 0.1f;
    public float resetTime = 1.5f;
    public boolean enabled = true;
    public boolean animations = true;
    public float maxWidth = 300f;
    public float offsetX = 10f;
    public float offsetY = 10f;

    public void instant(Element tooltip){
    }

    public void touch(Element tooltip){
    }
}
