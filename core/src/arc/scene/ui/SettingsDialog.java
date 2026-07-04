package arc.scene.ui;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;

public class SettingsDialog extends Dialog{
    public SettingsDialog(){
        super("");
    }

    public SettingsDialog(String title){
        super(title);
    }

    public static class SettingsTable extends Table{
        private Cons<SettingsTable> cons;

        public SettingsTable(){
        }

        public SettingsTable(Cons<SettingsTable> cons){
            this.cons = cons;
        }

        public void finish(){
            if(cons != null){
                cons.get(this);
            }
        }

        public void checkPref(String name, boolean def){
            Core.settings.defaults(name, def);
            CheckBox box = new CheckBox(Core.bundle.get(name));
            box.setChecked(Core.settings.getBool(name));
            box.changed(() -> {
                Core.settings.put(name, box.isChecked());
                Core.settings.manualSave();
            });
            add(box).left().padTop(5);
            row();
        }

        public <T> void checkPref(String name, boolean def, Cons<Boolean> changed){
            Core.settings.defaults(name, def);
            CheckBox box = new CheckBox(Core.bundle.get(name));
            box.setChecked(Core.settings.getBool(name));
            box.changed(() -> {
                Core.settings.put(name, box.isChecked());
                Core.settings.manualSave();
                changed.get(box.isChecked());
            });
            add(box).left().padTop(5);
            row();
        }

        public void sliderPref(String name, int def, int min, int max, Func<Integer, String> s){
            sliderPref(name, def, min, max, 1, s);
        }

        public void sliderPref(String name, int def, int min, int max, int step, Func<Integer, String> s){
            Core.settings.defaults(name, def);
            row();
            add(Core.bundle.get(name));
            Slider slider = new Slider(min, max, step, false);
            Label value = new Label("");
            add(slider).fillX();
            add(value).width(60);
            slider.setValue(Core.settings.getInt(name));
            slider.moved(val -> {
                int intVal = (int)val;
                Core.settings.put(name, intVal);
                Core.settings.manualSave();
                value.setText(s.get(intVal));
            });
            value.setText(s.get(Core.settings.getInt(name)));
            row();
        }

        public void volumePrefs(){
            sliderPref("music", 100, 0, 100, i -> i + "%");
            sliderPref("sfx", 100, 0, 100, i -> i + "%");
        }

        public void screenshakePref(){
            checkPref("screenshake", true);
        }

        public void pref(Setting setting){
            setting.add(this);
        }

        public Cell<TextButton> addButton(String text, Runnable listener){
            TextButton button = new TextButton(text);
            button.clicked(listener);
            return add(button);
        }

        public Cell<TextButton> addImageTextButton(String text, String icon, float iconSize, Runnable listener){
            TextButton button = new TextButton(text);
            button.clicked(listener);
            return add(button);
        }

        public static class Setting{
            public void add(SettingsTable table){
            }
        }
    }

    public void addSettings(){
    }
}
