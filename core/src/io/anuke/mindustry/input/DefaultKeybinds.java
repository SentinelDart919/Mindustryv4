package io.anuke.mindustry.input;

import arc.Application;
import arc.Application.ApplicationType;
import arc.Core;
import arc.input.KeyBind;
import arc.input.KeyBinds;
import arc.input.KeyBinds.Category;
import arc.input.KeyCode;
import arc.input.KeyCode.Keys;
import arc.scene.ui.DeviceType;

public class DefaultKeybinds{

    public static void load(){
        String[] sections = {"player_1"};

        for(String section : sections){

            KeyBinds.defaultSection(section, DeviceType.desktop,
                new Category("general"),
                "move_x", new KeyBind.Axis(KeyCode.a, KeyCode.d),
                "move_y", new KeyBind.Axis(KeyCode.s, KeyCode.w),
                "select", KeyCode.mouseLeft,
                "deselect", KeyCode.mouseRight,
                "break", KeyCode.mouseRight,
                "rotate", new KeyBind.Axis(KeyCode.scroll),
                "dash", KeyCode.shiftLeft,
                "drop_unit", KeyCode.shiftLeft,
                new Category("view"),
                "zoom_hold", KeyCode.controlLeft,
                "zoom", new KeyBind.Axis(KeyCode.scroll),
                "zoom_minimap", new KeyBind.Axis(KeyCode.minus, KeyCode.plus),
                "menu", Core.app.getType() == ApplicationType.android ? KeyCode.back : KeyCode.escape,
                "pause", KeyCode.space,
                "toggle_menus", KeyCode.c,
                "screenshot", KeyCode.p,
                "copy", KeyCode.f,
                "schematic_select", KeyCode.t,
                "schematic_flip_x", KeyCode.x,
                "schematic_flip_y", KeyCode.z,
                "map", KeyCode.m,
                new Category("multiplayer"),
                "player_list", KeyCode.tab,
                "chat", KeyCode.enter,
                "chat_history_prev", KeyCode.up,
                "chat_history_next", KeyCode.down,
                "chat_scroll", new KeyBind.Axis(KeyCode.scroll)
            );

            KeyBinds.defaultSection(section, DeviceType.controller,
                new Category("general"),
                "move_x", new KeyBind.Axis(KeyCode.controllerLStickXAxis),
                "move_y", new KeyBind.Axis(KeyCode.controllerLStickYAxis),
                "cursor_x", new KeyBind.Axis(KeyCode.controllerRStickXAxis),
                "cursor_y", new KeyBind.Axis(KeyCode.controllerRStickYAxis),
                //"select", KeyCode.controllerRBumper,
                //"break", KeyCode.controllerLBumper,
                //"shoot", KeyCode.controllerRTrigger,
                "dash", KeyCode.controllerY,
                "rotate_alt", new KeyBind.Axis(KeyCode.controllerdPadRight, KeyCode.controllerdPadLeft),
                "rotate", new KeyBind.Axis(KeyCode.controllerA, KeyCode.controllerB),
                new Category("view"),
                "zoom_hold", KeyCode.anyKey,
                "zoom", new KeyBind.Axis(KeyCode.controllerdPadDown, KeyCode.controllerdPadUp),
                "menu", KeyCode.controllerX,
                "pause", KeyCode.controllerLTrigger,
                new Category("multiplayer"),
                "player_list", KeyCode.controllerStart
            );

        }

        KeyBinds.setSectionAlias("default", "player_1");
    }
}
