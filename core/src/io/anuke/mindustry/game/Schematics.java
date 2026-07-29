package io.anuke.mindustry.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.Liquid;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.ucore.util.Log;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static io.anuke.mindustry.Vars.content;

public class Schematics {
    private Array<Schematic> all = new Array<>();
    private LongMap<Object> pendingConfigs = new LongMap<>();

    public void load() {
        all.clear();
        if (!Vars.schematicDirectory.exists()) return;

        for (FileHandle file : Vars.schematicDirectory.list()) {
            if (file.extension().equals("msch")) {
                try {
                    Schematic s = read(file);
                    all.add(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Array<Schematic> all() {
        return all;
    }

    public void save(Schematic schem) {
        if (!all.contains(schem, true)) all.add(schem);
        Vars.schematicDirectory.mkdirs();
        try {
            write(schem, Vars.schematicDirectory.child(schem.name() + ".msch"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(Schematic schem) {
        all.removeValue(schem, true);
        if (schem.file != null && schem.file.exists()) {
            schem.file.delete();
        }
    }

    public void place(Schematic schem, int x, int y, Team team) {
        for (Schematic.Stile stile : schem.tiles) {
            if (stile.block == null) continue;
            int ox = x + stile.x + (stile.block.size - 1) / 2;
            int oy = y + stile.y + (stile.block.size - 1) / 2;
            if(stile.config != null){
                pendingConfigs.put(key(ox, oy), stile.config);
            }
            Vars.control.input(0).tryPlaceBlock(ox, oy, Recipe.getByResult(stile.block), stile.rotation);
        }
    }

    public void applyConfig(Tile tile){
        long k = key(tile.x, tile.y);
        Object config = pendingConfigs.get(k);
        if(config != null){
            pendingConfigs.remove(k);
            if(tile.entity != null){
                tile.entity.configured(config);
            }
        }
    }

    private static long key(int x, int y){
        return ((long)x << 32) | (y & 0xffffffffL);
    }

    public Schematic create(int x, int y, int x2, int y2) {
        int minx = Math.min(x, x2);
        int miny = Math.min(y, y2);
        int maxx = Math.max(x, x2);
        int maxy = Math.max(y, y2);

        Array<Schematic.Stile> tiles = new Array<>();
        int minTileX = Integer.MAX_VALUE, minTileY = Integer.MAX_VALUE;
        int maxTileX = Integer.MIN_VALUE, maxTileY = Integer.MIN_VALUE;

        for (int ix = minx; ix <= maxx; ix++) {
            for (int iy = miny; iy <= maxy; iy++) {
                Tile tile = Vars.world.tile(ix, iy);
                if (tile == null || tile.block() == Blocks.air) continue;
                if (tile.isLinked()) continue;

                //Normalize to bottom-left
                int ox = ix - (tile.block().size - 1) / 2;
                int oy = iy - (tile.block().size - 1) / 2;

                tiles.add(new Schematic.Stile(tile.block(), ox, oy, tile.entity != null ? tile.entity.config() : null, tile.getRotation()));
                minTileX = Math.min(minTileX, ox);
                minTileY = Math.min(minTileY, oy);
                maxTileX = Math.max(maxTileX, ox + tile.block().size - 1);
                maxTileY = Math.max(maxTileY, oy + tile.block().size - 1);
            }
        }

        if (tiles.size == 0) return new Schematic(new Array<>(), new ObjectMap<>(), 0, 0);

        for (Schematic.Stile stile : tiles) {
            stile.x -= minTileX;
            stile.y -= minTileY;
        }

        int width = maxTileX - minTileX + 1;
        int height = maxTileY - minTileY + 1;

        ObjectMap<String, String> tags = new ObjectMap<>();
        tags.put("name", "Schematic " + (all.size + 1));
        return new Schematic(tiles, tags, width, height);
    }

    public Schematic read(FileHandle file) throws IOException {
        try (InputStream is = new InflaterInputStream(file.read())) {
            DataInputStream stream = new DataInputStream(is);
            Schematic s = read(stream);
            s.file = file;
            return s;
        }
    }

    public Schematic read(DataInputStream stream) throws IOException {
        byte[] header = new byte[4];
        stream.readFully(header);
        if (!new String(header).equals("MSCH")) throw new IOException("Not a schematic file");

        int version = stream.readUnsignedByte();
        short width = stream.readShort();
        short height = stream.readShort();

        int tagCount = stream.readUnsignedByte();
        ObjectMap<String, String> tags = new ObjectMap<>();
        for (int i = 0; i < tagCount; i++) {
            tags.put(stream.readUTF(), stream.readUTF());
        }

        int blockCount = stream.readUnsignedShort();
        Block[] blocks = new Block[blockCount];
        for (int i = 0; i < blockCount; i++) {
            String name = stream.readUTF();
            blocks[i] = content.getByName(ContentType.block, name);
            if(blocks[i] == null) Log.err("Block not found in schematic: {0}", name);
        }

        int tileCount = stream.readInt();
        Array<Schematic.Stile> tiles = new Array<>(tileCount);
        for (int i = 0; i < tileCount; i++) {
            int blockIndex = stream.readUnsignedByte();
            short x = stream.readShort();
            short y = stream.readShort();
            byte rotation = stream.readByte();
            Object config = version >= 2 ? readTileConfig(stream) : null;
            
            if(blockIndex < blocks.length && blocks[blockIndex] != null){
                tiles.add(new Schematic.Stile(blocks[blockIndex], x, y, config, rotation));
            }
        }

        return new Schematic(tiles, tags, width, height);
    }

    public void write(Schematic schematic, FileHandle file) throws IOException {
        try (OutputStream os = new DeflaterOutputStream(file.write(false))) {
            DataOutputStream stream = new DataOutputStream(os);
            write(schematic, stream);
        }
    }

    public void write(Schematic schematic, DataOutputStream stream) throws IOException {
        stream.writeBytes("MSCH");
        stream.writeByte(2); // version
        stream.writeShort(schematic.width);
        stream.writeShort(schematic.height);

        stream.writeByte(schematic.tags.size);
        for (ObjectMap.Entry<String, String> entry : schematic.tags.entries()) {
            stream.writeUTF(entry.key);
            stream.writeUTF(entry.value);
        }

        Array<Block> blocks = new Array<>();
        for (Schematic.Stile tile : schematic.tiles) {
            if (tile.block != null && !blocks.contains(tile.block, true)) {
                blocks.add(tile.block);
            }
        }

        stream.writeShort(blocks.size);
        for (Block block : blocks) {
            stream.writeUTF(block.name);
        }

        int count = 0;
        for(Schematic.Stile tile : schematic.tiles) if(tile.block != null) count++;
        stream.writeInt(count);

        for (Schematic.Stile tile : schematic.tiles) {
            if (tile.block == null) continue;
            stream.writeByte(blocks.indexOf(tile.block, true));
            stream.writeShort(tile.x);
            stream.writeShort(tile.y);
            stream.writeByte(tile.rotation);
            writeTileConfig(stream, tile.config);
        }
    }

    private void writeTileConfig(DataOutputStream stream, Object config) throws IOException {
        if (config == null) {
            stream.writeByte(0);
        } else if (config instanceof Item) {
            stream.writeByte(1);
            stream.writeShort(((Item) config).id);
        } else if (config instanceof Liquid) {
            stream.writeByte(2);
            stream.writeShort(((Liquid) config).id);
        } else if (config instanceof Integer) {
            stream.writeByte(3);
            stream.writeInt((Integer) config);
        } else if (config instanceof int[]) {
            stream.writeByte(4);
            int[] arr = (int[]) config;
            stream.writeShort(arr.length);
            for (int i : arr) stream.writeInt(i);
        } else {
            stream.writeByte(0);
        }
    }

    private Object readTileConfig(DataInputStream stream) throws IOException {
        byte type = stream.readByte();
        switch (type) {
            case 0: return null;
            case 1: return Vars.content.item(stream.readShort());
            case 2: return Vars.content.liquid(stream.readShort());
            case 3: return stream.readInt();
            case 4: {
                int len = stream.readUnsignedShort();
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) arr[i] = stream.readInt();
                return arr;
            }
            default: return null;
        }
    }

    public String writeBase64(Schematic schematic) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(schematic, new DataOutputStream(new DeflaterOutputStream(out)));
        return new String(Base64Coder.encode(out.toByteArray()));
    }

    public Schematic readBase64(String base64) throws IOException {
        byte[] bytes = Base64Coder.decode(base64);
        return read(new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes))));
    }
}
