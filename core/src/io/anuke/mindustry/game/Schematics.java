package io.anuke.mindustry.game;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.serialization.Base64Coder;
import arc.struct.ObjectMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.content.blocks.Blocks;
import arc.util.Log;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static io.anuke.mindustry.Vars.content;

public class Schematics {
    private Seq<Schematic> all = new Seq<>();

    public void load() {
        all.clear();
        if (!Vars.schematicDirectory.exists()) return;

        for (Fi file : Vars.schematicDirectory.list()) {
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

    public Seq<Schematic> all() {
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
        all.remove(schem, true);
        if (schem.file != null && schem.file.exists()) {
            schem.file.delete();
        }
    }

    public void place(Schematic schem, int x, int y, Team team) {
        for (Schematic.Stile tile : schem.tiles) {
            if (tile.block == null) continue;
            //Convert bottom-left back to v4 origin
            int ox = x + tile.x + (tile.block.size - 1) / 2;
            int oy = y + tile.y + (tile.block.size - 1) / 2;
            Vars.control.input(0).tryPlaceBlock(ox, oy, Recipe.getByResult(tile.block), tile.rotation);
        }
    }

    public Schematic create(int x, int y, int x2, int y2) {
        int minx = Math.min(x, x2);
        int miny = Math.min(y, y2);
        int maxx = Math.max(x, x2);
        int maxy = Math.max(y, y2);

        Seq<Schematic.Stile> tiles = new Seq<>();
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

                tiles.add(new Schematic.Stile(tile.block(), ox, oy, null, tile.getRotation()));
                minTileX = Math.min(minTileX, ox);
                minTileY = Math.min(minTileY, oy);
                maxTileX = Math.max(maxTileX, ox + tile.block().size - 1);
                maxTileY = Math.max(maxTileY, oy + tile.block().size - 1);
            }
        }

        if (tiles.size == 0) return new Schematic(new Seq<>(), new ObjectMap<>(), 0, 0);

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

    public Schematic read(Fi file) throws IOException {
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
        Seq<Schematic.Stile> tiles = new Seq<>(tileCount);
        for (int i = 0; i < tileCount; i++) {
            int blockIndex = stream.readUnsignedByte();
            short x = stream.readShort();
            short y = stream.readShort();
            byte rotation = stream.readByte();
            
            if(blockIndex < blocks.length && blocks[blockIndex] != null){
                tiles.add(new Schematic.Stile(blocks[blockIndex], x, y, null, rotation));
            }
        }

        return new Schematic(tiles, tags, width, height);
    }

    public void write(Schematic schematic, Fi file) throws IOException {
        try (OutputStream os = new DeflaterOutputStream(file.write(false))) {
            DataOutputStream stream = new DataOutputStream(os);
            write(schematic, stream);
        }
    }

    public void write(Schematic schematic, DataOutputStream stream) throws IOException {
        stream.writeBytes("MSCH");
        stream.writeByte(1); // version
        stream.writeShort(schematic.width);
        stream.writeShort(schematic.height);

        stream.writeByte(schematic.tags.size);
        for (ObjectMap.Entry<String, String> entry : schematic.tags.entries()) {
            stream.writeUTF(entry.key);
            stream.writeUTF(entry.value);
        }

        Seq<Block> blocks = new Seq<>();
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

