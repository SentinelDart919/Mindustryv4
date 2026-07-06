package arc.graphics.g2d;

import arc.*;
import arc.Files.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import java.io.*;

/**
 * Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet
 */
public class TextureAtlas implements Disposable{
    private final ObjectSet<Texture> textures = new ObjectSet<>(4);
    private final Seq<AtlasRegion> regions = new Seq<>(false);
    private final ObjectMap<String, Drawable> drawables = new ObjectMap<>();
    private final ObjectMap<String, AtlasRegion> regionmap = new ObjectMap<>();
    private final ObjectMap<Texture, Pixmap> pixmaps = new ObjectMap<>();
    protected AtlasRegion error, white;
    protected float drawableScale = 1f;

    /** Returns a new texture atlas with only a blank texture region.*/
    public static TextureAtlas blankAtlas(){
        TextureAtlas a =  new TextureAtlas();
        a.white = new AtlasRegion(Pixmaps.blankTextureRegion());
        return a;
    }

    /** Creates an empty atlas to which regions can be added. */
    public TextureAtlas(){
    }

    /**
     * Loads the specified pack file using {@link FileType#internal}, using the parent directory of the pack file to find the page
     * images.
     */
    public TextureAtlas(String internalPackFile){
        this(Core.files.internal(internalPackFile));
    }

    /** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
    public TextureAtlas(Fi packFile){
        this(packFile, packFile.parent());
    }

    /**
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     * @see #TextureAtlas(Fi)
     */
    public TextureAtlas(Fi packFile, boolean flip){
        this(packFile, packFile.parent(), flip);
    }

    public TextureAtlas(Fi packFile, Fi imagesDir){
        this(packFile, imagesDir, false);
    }

    /** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
    public TextureAtlas(Fi packFile, Fi imagesDir, boolean flip){
        this(new TextureAtlasData(packFile, imagesDir, flip));
    }

    /** @param data May be null. */
    public TextureAtlas(TextureAtlasData data){
        if(data != null) load(data);
    }

    public void setDrawableScale(float scale){
        this.drawableScale = scale;
    }

    private void load(TextureAtlasData data){
        ObjectMap<AtlasPage, Texture> pageToTexture = new ObjectMap<>();
        for(AtlasPage page : data.pages){
            Texture texture;
            if(page.texture == null){
                texture = new Texture(page.textureFile, page.useMipMaps);
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }else{
                texture = page.texture;
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }
            textures.add(texture);
            pageToTexture.put(page, texture);
        }

        for(Region region : data.regions){
            int width = region.width;
            int height = region.height;
            AtlasRegion atlasRegion = new AtlasRegion(pageToTexture.get(region.page), region.left, region.top,
            region.rotate ? height : width, region.rotate ? width : height);
            atlasRegion.name = region.name;
            atlasRegion.offsetX = region.offsetX;
            atlasRegion.offsetY = region.offsetY;
            atlasRegion.originalHeight = region.originalHeight;
            atlasRegion.originalWidth = region.originalWidth;
            atlasRegion.rotate = region.rotate;
            atlasRegion.splits = region.splits;
            atlasRegion.pads = region.pads;
            if(region.flip) atlasRegion.flip(false, true);
            regions.add(atlasRegion);
            regionmap.put(atlasRegion.name, atlasRegion);
        }

        error = find("error");
    }

    public PixmapRegion getPixmap(String name){
        return getPixmap(find(name));
    }

    public PixmapRegion getPixmap(AtlasRegion region){
        if(region.pixmapRegion == null){
            Pixmap pix = pixmaps.get(region.texture, () -> region.texture.getTextureData().getPixmap());
            region.pixmapRegion = new PixmapRegion(pix, region.getX(), region.getY(), region.width, region.height);
        }

        return region.pixmapRegion;
    }

    public PixmapRegion getPixmap(TextureRegion region){
        return getPixmap((AtlasRegion)region);
    }

    public ObjectMap<Texture, Pixmap> getPixmaps(){
        return pixmaps;
    }

    public void disposePixmap(Texture texture){
        if(pixmaps.containsKey(texture) && !pixmaps.get(texture).isDisposed()){
            pixmaps.get(texture).dispose();
        }
        pixmaps.remove(texture);
    }

    /** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
        textures.add(texture);
        AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
        region.name = name;
        region.originalWidth = width;
        region.originalHeight = height;
        regions.add(region);
        regionmap.put(name, region);
        return region;
    }

    /** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion(String name, TextureRegion textureRegion){
        return addRegion(name, textureRegion.texture, textureRegion.getX(), textureRegion.getY(),
        textureRegion.width, textureRegion.height);
    }

    /** Returns all regions in the atlas. */
    public Seq<AtlasRegion> getRegions(){
        return regions;
    }

    /** Returns the region map in the atlas. */
    public ObjectMap<String, AtlasRegion> getRegionMap(){
        return regionmap;
    }

    /** Returns the blank 1x1 texture region, if it exists.*/
    public AtlasRegion white(){
        if(white == null){
            white = find("white");
        }
        return white;
    }

    /** Finds and sets error region as name. */
    public boolean setErrorRegion(String name) {
        if(error != null || !has(name)) return false;
        error = find(name);
        return true;
    }

    public boolean isFound(TextureRegion region){
        return region != error;
    }

    /**
     * Returns the first region found with the specified name. This method's performance is no longer garbage.
     * @return The region, or the error region (if it is defined), or null.
     */
    public AtlasRegion find(String name){
        AtlasRegion r = regionmap.get(name, error);
        if(r == null && !name.equals("error"))
            throw new IllegalArgumentException("The region \"" + name + "\" does not exist!");
        return r;
    }

    public TextureRegion find(String name, String def){
        return find(name, find(def));
    }

    public TextureRegion find(String name, TextureRegion def){
        TextureRegion region = regionmap.get(name);
        return region == null || region == error ? def : region;
    }

    public boolean has(String s){
        return regionmap.containsKey(s);
    }

    @SuppressWarnings("unchecked")
    public <T extends Drawable> T getDrawable(String name){
        return (T)drawable(name);
    }

    /** Always creates a new drawable by name.
     * If nothing is found, returns an 'error' texture region drawable. */
    public Drawable drawable(String name){
        if(drawables.containsKey(name)){
            return drawables.get(name);
        }

        Drawable out = null;

        if(has(name)){
            AtlasRegion region = find(name);

            if(region.splits != null){
                int[] splits = region.splits;
                NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                int[] pads = region.pads;
                if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                out = new ScaledNinePatchDrawable(patch, drawableScale);
            }else{
                out = new TextureRegionDrawable(region, drawableScale);
            }
        }

        if(error == null && out == null) throw new IllegalArgumentException("No drawable '" + name + "' found.");
        if(out == null) out = new TextureRegionDrawable(error);
        drawables.put(name, out);

        return out;
    }

    /**
     * Returns the first region found with the specified name as a {@link NinePatch}. The region must have been packed with
     * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
     * be cached rather than calling this method multiple times.
     * @return The ninepatch, or null.
     */
    public NinePatch createPatch(String name){
        for(int i = 0, n = regions.size; i < n; i++){
            AtlasRegion region = regions.get(i);
            if(region.name.equals(name)){
                int[] splits = region.splits;
                if(splits == null) throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
                NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                if(region.pads != null)
                    patch.setPadding(region.pads[0], region.pads[1], region.pads[2], region.pads[3]);
                return patch;
            }
        }
        return null;
    }

    /** @return the textures of the pages, unordered */
    public ObjectSet<Texture> getTextures(){
        return textures;
    }

    /** @return the first texture of the pages.*/
    public Texture texture(){
        return textures.first();
    }

    /**
     * Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
     * and Sprites, which should no longer be used after calling dispose.
     */
    public void dispose(){
        for(Texture texture : textures)
            texture.dispose();
        for(Pixmap pixmap : pixmaps.values())
            if(!pixmap.isDisposed())
                pixmap.dispose();
        textures.clear();
        pixmaps.clear();
    }

    public static class TextureAtlasData{
        public static final byte formatVersion = 0;
        public static final byte[] formatHeader = new byte[]{'A', 'A', 'T', 'L', 'S'};

        final Seq<AtlasPage> pages = new Seq<>();
        final Seq<Region> regions = new Seq<>();

        public TextureAtlasData(Fi packFile, Fi imagesDir, boolean flip){
            try{
                byte[] data = packFile.readBytes();
                if(isBinaryAtlas(data)){
                    parseBinary(new Reads(new DataInputStream(new ByteArrayInputStream(data))), packFile, imagesDir, flip);
                }else{
                    parseText(packFile.readString(), imagesDir, flip);
                }
            }catch(Exception e){
                throw new ArcRuntimeException("Error reading pack file: " + packFile, e);
            }
        }

        private boolean isBinaryAtlas(byte[] data){
            if(data.length < formatHeader.length + 1) return false;
            for(int i = 0; i < formatHeader.length; i++){
                if(data[i] != formatHeader[i]) return false;
            }
            return true;
        }

        private void parseBinary(Reads read, Fi packFile, Fi imagesDir, boolean flip){
            try(read){
                for(byte b : formatHeader){
                    if(read.b() != b){
                        throw new IOException("Invalid binary header. Have you re-packed sprites?");
                    }
                }
                //discard version
                read.b();

                while(read.checkEOF() != -1){
                    String image = read.str();
                    Fi file = imagesDir.child(image);

                    short pageWidth = read.s(), pageHeight = read.s();

                    TextureFilter min = TextureFilter.all[read.b()], mag = TextureFilter.all[read.b()];
                    TextureWrap wrapX = TextureWrap.all[read.b()], wrapY = TextureWrap.all[read.b()];

                    int rects = read.i();

                    AtlasPage page = new AtlasPage(file, pageWidth, pageHeight, min.isMipMap(), min, mag, wrapX, wrapY);
                    pages.add(page);

                    for(int j = 0; j < rects; j++){
                        Region region = new Region();
                        region.flip = flip;
                        region.page = page;
                        region.name = read.str();
                        region.left = read.s();
                        region.top = read.s();
                        region.width = read.s();
                        region.height = read.s();

                        if(read.bool()){
                            region.offsetX = read.s();
                            region.offsetY = read.s();
                            region.originalWidth = read.s();
                            region.originalHeight = read.s();
                        }

                        if(read.bool()){
                            region.splits = new int[]{read.s(), read.s(), read.s(), read.s()};
                        }

                        if(read.bool()){
                            region.pads = new int[]{read.s(), read.s(), read.s(), read.s()};
                        }

                        regions.add(region);
                    }
                }
            }catch(IOException e){
                throw new ArcRuntimeException("Error reading pack file: " + packFile, e);
            }
        }

        private void parseText(String text, Fi imagesDir, boolean flip) throws IOException{
            String[] lines = text.replace("\r", "").split("\n");
            PageState state = null;
            AtlasPage page = null;
            Region region = null;

            for(String raw : lines){
                if(raw == null) continue;
                String line = raw.trim();
                if(line.isEmpty()) continue;

                boolean indented = raw.startsWith(" ") || raw.startsWith("\t");
                if(!indented){
                    if(line.endsWith(".png")){
                        state = new PageState(line, imagesDir.child(line));
                        page = null;
                        region = null;
                    }else{
                        if(state == null) throw new IOException("Region found before page header: " + line);
                        if(page == null){
                            page = state.create();
                            pages.add(page);
                        }
                        region = new Region();
                        region.flip = flip;
                        region.page = page;
                        region.name = line;
                        regions.add(region);
                    }
                    continue;
                }

                if(state == null) throw new IOException("Property found before page header: " + line);
                if(region == null){
                    state.readPageProperty(line);
                }else{
                    readRegionProperty(region, line);
                }
            }
        }

        private void readRegionProperty(Region region, String line) throws IOException{
            if(line.startsWith("rotate:")){
                region.rotate = Boolean.parseBoolean(line.substring(7).trim());
            }else if(line.startsWith("xy:")){
                int[] values = parseInts(line.substring(3), 2);
                region.left = values[0];
                region.top = values[1];
            }else if(line.startsWith("size:")){
                int[] values = parseInts(line.substring(5), 2);
                region.width = values[0];
                region.height = values[1];
            }else if(line.startsWith("orig:")){
                int[] values = parseInts(line.substring(5), 2);
                region.originalWidth = values[0];
                region.originalHeight = values[1];
            }else if(line.startsWith("offset:")){
                int[] values = parseInts(line.substring(7), 2);
                region.offsetX = values[0];
                region.offsetY = values[1];
            }else if(line.startsWith("split:")){
                region.splits = parseInts(line.substring(6), 4);
            }else if(line.startsWith("pad:")){
                region.pads = parseInts(line.substring(4), 4);
            }
        }

        private int[] parseInts(String value, int count) throws IOException{
            String[] parts = value.split(",");
            if(parts.length < count) throw new IOException("Invalid atlas value: " + value);
            int[] out = new int[count];
            for(int i = 0; i < count; i++){
                out[i] = Integer.parseInt(parts[i].trim());
            }
            return out;
        }

        private TextureFilter parseFilter(String value){
            return TextureFilter.valueOf(value.trim().toLowerCase(java.util.Locale.ROOT));
        }

        private TextureWrap parseWrap(String value){
            value = value.trim().toLowerCase(java.util.Locale.ROOT);
            return switch(value){
                case "x", "y", "both", "repeat" -> TextureWrap.repeat;
                case "mirroredrepeat" -> TextureWrap.mirroredRepeat;
                default -> TextureWrap.clampToEdge;
            };
        }

        private class PageState{
            final String name;
            final Fi file;
            int width;
            int height;
            TextureFilter min = TextureFilter.nearest;
            TextureFilter mag = TextureFilter.nearest;
            TextureWrap wrapX = TextureWrap.clampToEdge;
            TextureWrap wrapY = TextureWrap.clampToEdge;

            PageState(String name, Fi file){
                this.name = name;
                this.file = file;
            }

            void readPageProperty(String line) throws IOException{
                if(line.startsWith("size:")){
                    int[] values = parseInts(line.substring(5), 2);
                    width = values[0];
                    height = values[1];
                }else if(line.startsWith("filter:")){
                    String[] values = line.substring(7).split(",");
                    if(values.length < 2) throw new IOException("Invalid filter line in atlas: " + line);
                    min = parseFilter(values[0]);
                    mag = parseFilter(values[1]);
                }else if(line.startsWith("repeat:")){
                    TextureWrap wrap = parseWrap(line.substring(7));
                    wrapX = wrap;
                    wrapY = wrap;
                }
            }

            AtlasPage create(){
                return new AtlasPage(file, width, height, min.isMipMap(), min, mag, wrapX, wrapY);
            }
        }

        public Seq<AtlasPage> getPages(){
            return pages;
        }

        public Seq<Region> getRegions(){
            return regions;
        }

        public static class AtlasPage{
            public final Fi textureFile;
            public final int width, height;
            public final boolean useMipMaps;
            public final TextureFilter minFilter;
            public final TextureFilter magFilter;
            public final TextureWrap uWrap;
            public final TextureWrap vWrap;
            public Texture texture;

            public AtlasPage(Fi handle, int width, int height, boolean useMipMaps, TextureFilter minFilter,
                             TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap){
                this.width = width;
                this.height = height;
                this.textureFile = handle;
                this.useMipMaps = useMipMaps;
                this.minFilter = minFilter;
                this.magFilter = magFilter;
                this.uWrap = uWrap;
                this.vWrap = vWrap;
            }
        }

        public static class Region{
            public AtlasPage page;
            public String name;
            public float offsetX;
            public float offsetY;
            public int originalWidth;
            public int originalHeight;
            public boolean rotate;
            public int left;
            public int top;
            public int width;
            public int height;
            public boolean flip;
            public int[] splits;
            public int[] pads;
        }
    }

    /** Describes the region of a packed image and provides information about the original image before it was packed. */
    public static class AtlasRegion extends TextureRegion{
        public PixmapRegion pixmapRegion;

        /**
         * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
         * packer.
         */
        public String name;

        /** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
         * packing.
         */
        public float offsetY;

        /** The width of the image, after whitespace was removed for packing. */
        public int packedWidth;

        /** The height of the image, after whitespace was removed for packing. */
        public int packedHeight;

        /** The width of the image, before whitespace was removed and rotation was applied for packing. */
        public int originalWidth;

        /** The height of the image, before whitespace was removed for packing. */
        public int originalHeight;

        /** If true, the region has been rotated 90 degrees counter clockwise. */
        public boolean rotate;

        /** The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom. */
        public int[] splits;

        /** The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom. */
        public int[] pads;

        public AtlasRegion(Texture texture, int x, int y, int width, int height){
            super(texture, x, y, width, height);
            originalWidth = width;
            originalHeight = height;
            packedWidth = width;
            packedHeight = height;
        }

        public AtlasRegion(){

        }

        public AtlasRegion(AtlasRegion region){
            set(region);
            name = region.name;
            offsetX = region.offsetX;
            offsetY = region.offsetY;
            packedWidth = region.packedWidth;
            packedHeight = region.packedHeight;
            originalWidth = region.originalWidth;
            originalHeight = region.originalHeight;
            rotate = region.rotate;
            splits = region.splits;
        }

        public AtlasRegion(TextureRegion region){
            set(region);
            name = "unknown";
        }

        /** Flips the region, adjusting the offset so the image appears to be flip as if no whitespace has been removed for packing. */
        @Override
        public void flip(boolean x, boolean y){
            super.flip(x, y);
            if(x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
            if(y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
        }

        /**
         * Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
         * returns the packedWidth.
         */
        public float getRotatedPackedWidth(){
            return rotate ? packedHeight : packedWidth;
        }

        /**
         * Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
         * returns the packedHeight.
         */
        public float getRotatedPackedHeight(){
            return rotate ? packedWidth : packedHeight;
        }

        public String toString(){
            return name;
        }
    }
}
