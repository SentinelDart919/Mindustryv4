package io.anuke.mindustry.ui.fragments;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.util.Align;
import arc.util.Inputs;
import arc.struct.Seq;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.net.Net;
import arc.Core;
import arc.Input;
import arc.util.Time;
import arc.util.Timers;
import arc.scene.Group;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.*;
import static arc.Core.scene;

public class ChatFragment extends Table{
    private final static int messagesShown = 10;
    private Seq<ChatMessage> messages = new Seq<>();
    private float fadetime;
    private boolean chatOpen = false;
    private TextField chatfield;
    private Label fieldlabel;
    private Font font;
    private GlyphLayout layout = new GlyphLayout();
    private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
    private float textWidth = Scl.scl(600);
    private Color shadowColor = new Color(0, 0, 0, 0.4f);
    private float textspacing = Scl.scl(10);
    private Seq<String> history = new Seq<>();
    private int historyPos = 0;
    private int scrollPos = 0;
    private Fragment container = new Fragment(){
        @Override
        public void build(Group parent){
            setup();
            scene.add(ChatFragment.this);
        }
    };

    public ChatFragment(){
        super();

        setFillParent(true);

        visible(() -> {
            if(!Net.active() && messages.size > 0){
                clearMessages();

                if(chatOpen){
                    hide();
                }
            }

            return !state.is(State.menu) && Net.active();
        });

        update(() -> {

            if(Net.active() && Inputs.keyTap("chat")){
                toggle();
            }

            if(chatOpen){
                if(Inputs.keyTap("chat_history_prev") && historyPos < history.size - 1){
                    if(historyPos == 0) history.set(0, chatfield.getText());
                    historyPos++;
                    updateChat();
                }
                if(Inputs.keyTap("chat_history_next") && historyPos > 0){
                    historyPos--;
                    updateChat();
                }
                scrollPos = (int)Mathf.clamp(scrollPos + Core.input.axis(KeyCode.scroll), 0, Math.max(0, messages.size - messagesShown));
            }
        });

        history.insert(0, "");
    }

    public Fragment container(){
        return container;
    }

    public void clearMessages(){
        messages.clear();
        history.clear();
        history.insert(0, "");
    }

    private void setup(){
        if(fieldlabel != null) return;

        font = Core.scene.getSkin().getFont("default-font");
        fieldlabel = new Label(">");
        fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
        fieldlabel.getStyle().font = font;
        fieldlabel.setStyle(fieldlabel.getStyle());

        chatfield = new TextField("", new TextField.TextFieldStyle(scene.getSkin().get(TextField.TextFieldStyle.class)));
        chatfield.setTextFieldFilter((field, c) -> field.getText().length() < Vars.maxTextLength);
        chatfield.getStyle().background = null;
        chatfield.getStyle().font = scene.getSkin().getFont("default-font-chat");
        chatfield.getStyle().fontColor = Color.white;
        chatfield.setStyle(chatfield.getStyle());

        if(mobile){
            chatfield.tapped(() -> {
                Dialog dialog = new Dialog("", "dialog");
                dialog.setFillParent(true);
                dialog.content().top();
                dialog.content().defaults().height(65f);
                TextField to = dialog.content().addField(chatfield.getText() == null ? "" : chatfield.getText(), t-> {}).pad(15).width(250f).get();
                to.setMaxLength(maxTextLength);
                to.keyDown(KeyCode.enter, () -> {
                    if(dialog.content().find("okb") != null){
                        dialog.content().find("okb").fireClick();
                    }
                });
                dialog.content().addButton("$text.ok", () -> {
                    if(to == null || chatfield == null) return;
                    chatfield.clearText();
                    chatfield.appendText(to.getText() == null ? "" : to.getText());
                    chatfield.change();
                    dialog.hide();
                    Core.input.setOnscreenKeyboardVisible(false);
                    toggle();
                }).width(90f).name("okb");

                dialog.show();
                Timers.runTask(1f, () -> {
                    if(to == null || to.getScene() == null) return;
                    to.setCursorPosition(to.getText() == null ? 0 : to.getText().length());
                    Core.scene.setKeyboardFocus(to);
                    Core.input.setOnscreenKeyboardVisible(true);
                });
            });
        }

        bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);

        add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);

        if(Vars.mobile){
            marginBottom(105f);
            marginRight(240f);
        }
    }

    @Override
    public void draw(){
        Draw.color(shadowColor);

        if(chatOpen){
            Draw.rect("white", offsetx, chatfield.y, chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
        }

        super.draw();

        float spacing = chatspace;

        chatfield.visible = chatOpen;
        fieldlabel.visible = chatOpen;

        Draw.color(shadowColor);

        float theight = offsety + spacing + getMarginBottom();
        for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos && (i < fadetime || chatOpen); i++){

            layout.setText(font, messages.get(i).formattedMessage, Color.white, textWidth, Align.bottomLeft, true);
            theight += layout.height + textspacing;
            if(i - scrollPos == 0) theight -= textspacing + 1;

            font.getCache().clear();
            font.getCache().addText(messages.get(i).formattedMessage, fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

            if(!chatOpen && fadetime - i < 1f && fadetime - i >= 0f){
                font.getCache().setAlphas(fadetime - i);
                Draw.color(0, 0, 0, shadowColor.a * (fadetime - i));
            }

            Draw.rect("white", offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
            Draw.color(shadowColor);

            font.getCache().draw();
        }

        Draw.color(Color.white);

        if(fadetime > 0 && !chatOpen)
            fadetime -= Timers.delta() / 180f;
    }

    private void sendMessage(){
        String message = chatfield.getText();
        clearChatInput();

        if(message.replaceAll(" ", "").isEmpty()) return;

        history.insert(1, message);

        Call.sendMessage(players[0], message);
    }

    public void toggle(){

        if(!chatOpen){
            scene.setKeyboardFocus(chatfield);
            chatfield.fireClick();
            chatOpen = !chatOpen;
        }else{
            scene.setKeyboardFocus(null);
            chatOpen = !chatOpen;
            scrollPos = 0;
            sendMessage();
        }
    }

    public void hide(){
        scene.setKeyboardFocus(null);
        chatOpen = false;
        clearChatInput();
    }

    public void updateChat(){
        chatfield.setText(history.get(historyPos));
        chatfield.setCursorPosition(chatfield.getText().length());
    }

    public void clearChatInput(){
        historyPos = 0;
        history.set(0, "");
        chatfield.setText("");
    }

    public boolean chatOpen(){
        return chatOpen;
    }

    public int getMessagesSize(){
        return messages.size;
    }

    public void addMessage(String message, String sender){
        messages.insert(0, new ChatMessage(message, sender));

        fadetime += 1f;
        fadetime = Math.min(fadetime, messagesShown) + 1f;
    }

    private static class ChatMessage{
        public final String sender;
        public final String message;
        public final String formattedMessage;

        public ChatMessage(String message, String sender){
            this.message = message;
            this.sender = sender;
            if(sender == null){ //no sender, this is a server message?
                formattedMessage = message;
            }else{
                formattedMessage = "[CORAL][[" + sender + "[CORAL]]:[WHITE] " + message;
            }
        }
    }

}


