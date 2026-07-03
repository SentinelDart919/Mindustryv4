package arc.scene.ui;

import arc.scene.ui.layout.Table;

public class SettingsDialog extends Dialog{
    public SettingsDialog(){
        super("");
    }

    public SettingsDialog(String title){
        super(title);
    }

    public static class SettingsTable extends Table{
        public SettingsTable(){
        }

        public static class Setting{
        }
    }

    public void addSettings(){
    }
}
