package io.anuke.mindustry.core;

import arc.struct.ObjectIntMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.type.Item;
import arc.Core;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.world;

public class LaunchManager {
    private ObjectIntMap<Item> inventory = new ObjectIntMap<>();

    public void load() {
        inventory.clear();
        String prefix = getPrefix();
        for (Item item : content.items()) {
            int amount = Core.settings.getInt(prefix + item.name, 0);
            if (amount > 0) {
                inventory.put(item, amount);
            }
        }
    }

    public void save() {
        String prefix = getPrefix();
        for (Item item : content.items()) {
            int amount = inventory.get(item, 0);
            if (amount > 0) {
                Core.settings.put(prefix + item.name, amount);
            }
        }
        Core.settings.manualSave();
    }

    private String getPrefix() {
        if (world == null || world.sectors == null) {
            return "launch-item-";
        }
        String campaign = world.sectors.getActiveCampaign();
        if (campaign != null) {
            return "launch-item-" + campaign + "-";
        }
        return "launch-item-";
    }

    public int getAmount(Item item) {
        return inventory.get(item, 0);
    }

    public void addItems(Item item, int amount) {
        int current = inventory.get(item, 0);
        inventory.put(item, current + amount);
    }

    public boolean canAdd(Item item, int amount) {
        return inventory.get(item, 0) + amount <= getCapacity();
    }

    public void removeItems(Item item, int amount) {
        int current = inventory.get(item, 0);
        inventory.put(item, Math.max(0, current - amount));
    }

    public int getTotalItems() {
        int total = 0;
        for (ObjectIntMap.Entry<Item> entry : inventory.entries()) {
            total += entry.value;
        }
        return total;
    }

    public int getCapacity() {
        int completed = 0;
        if (world.sectors != null && world.sectors.activeGrid() != null) {
            for (Sector sector : world.sectors.activeGrid().values()) {
                if (sector.complete) {
                    completed++;
                }
            }
        }
        return completed * 500;
    }

    public ObjectIntMap<Item> getInventory() {
        return inventory;
    }
}
