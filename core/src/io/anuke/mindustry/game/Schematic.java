package io.anuke.mindustry.game;

import arc.files.Fi;
import arc.struct.Seq;
import arc.struct.ObjectMap;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;

import static io.anuke.mindustry.Vars.schematics;

public class Schematic {
    public final Seq<Stile> tiles;
    public ObjectMap<String, String> tags;
    public int width, height;
    public Fi file;

    public Schematic(Seq<Stile> tiles, ObjectMap<String, String> tags, int width, int height) {
        this.tiles = tiles;
        this.tags = tags;
        this.width = width;
        this.height = height;
    }

    public String name() {
        return tags.get("name", "unknown");
    }

    public String description() {
        return tags.get("description", "");
    }

    public void save() {
        schematics.save(this);
    }

    public void rotate() {
        for (Stile tile : tiles) {
            if (tile.block == null) continue;
            int temp = tile.x;
            tile.x = (short) (height - tile.block.size - tile.y);
            tile.y = (short) temp;
            tile.rotation = (byte) ((tile.rotation + 1) % 4);
        }

        int temp = width;
        width = height;
        height = temp;
    }

    public void flipX() {
        for (Stile tile : tiles) {
            if (tile.block == null) continue;
            tile.x = (short) (width - tile.block.size - tile.x);
            if (tile.rotation % 2 == 0) {
                tile.rotation = (byte) ((tile.rotation + 2) % 4);
            }
        }
    }

    public void flipY() {
        for (Stile tile : tiles) {
            if (tile.block == null) continue;
            tile.y = (short) (height - tile.block.size - tile.y);
            if (tile.rotation % 2 != 0) {
                tile.rotation = (byte) ((tile.rotation + 2) % 4);
            }
        }
    }

    public Schematic copy() {
        Seq<Stile> newTiles = new Seq<>(tiles.size);
        for (Stile tile : tiles) {
            newTiles.add(tile.copy());
        }
        return new Schematic(newTiles, new ObjectMap<>(tags), width, height);
    }

    public Seq<ItemStack> requirements() {
        ObjectMap<Item, Integer> reqs = new ObjectMap<>();
        for (Stile tile : tiles) {
            Recipe recipe = Recipe.getByResult(tile.block);
            if (recipe != null && recipe.requirements != null) {
                for (ItemStack stack : recipe.requirements) {
                    reqs.put(stack.item, reqs.get(stack.item, 0) + stack.amount);
                }
            }
        }
        Seq<ItemStack> result = new Seq<>();
        for (ObjectMap.Entry<Item, Integer> entry : reqs.entries()) {
            result.add(new ItemStack(entry.key, entry.value));
        }
        return result;
    }

    public static class Stile {
        public Block block;
        public short x, y;
        public Object config;
        public byte rotation;

        public Stile(Block block, int x, int y, Object config, byte rotation) {
            this.block = block;
            this.x = (short) x;
            this.y = (short) y;
            this.config = config;
            this.rotation = rotation;
        }

        public Stile copy() {
            return new Stile(block, x, y, config, rotation);
        }
    }
}
