package io.anuke.mindustry;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.g2d.PixmapPacker;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Log.NoopLogHandler;
import arc.util.Timers;
import io.anuke.mindustry.core.ContentLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ImageContext {
    private final ObjectMap<String, GenRegion> regionCache = new ObjectMap<>();
    private final Fi sourceRoot = Fi.get("core/assets-raw/sprites");
    private final Fi outputRoot = Fi.get("core/assets/sprites");

    public void load() throws IOException{
        Log.logger = new NoopLogHandler();
        try{
            loadSourceImages();
            installAtlas();

            Vars.content = new ContentLoader();
            Vars.content.load();
        }finally{
            Log.logger = new Log.DefaultLogHandler();
        }
    }

    private void loadSourceImages() throws IOException{
        if(!sourceRoot.exists()){
            throw new IOException("Missing sprite source directory: " + sourceRoot);
        }

        try(Stream<Path> paths = java.nio.file.Files.walk(sourceRoot.file().toPath())){
            List<File> files = new ArrayList<>();
            paths.filter(path -> java.nio.file.Files.isRegularFile(path))
            .filter(path -> path.toString().toLowerCase().endsWith(".png"))
            .forEach(path -> files.add(path.toFile()));

            files.sort(Comparator.comparing(file -> sourceRoot.file().toPath().relativize(file.toPath()).toString()));

            for(File file : files){
                BufferedImage image = ImageIO.read(file);
                if(image == null){
                    throw new IOException("Unable to read sprite image: " + file);
                }

                String name = file.getName();
                if(name.toLowerCase().endsWith(".png")){
                    name = name.substring(0, name.length() - 4);
                }

                GenRegion region = new GenRegion();
                region.name = name;
                region.context = this;
                region.source = image;
                region.set(0, 0, image.getWidth(), image.getHeight());

                GenRegion existing = regionCache.get(name);
                if(existing == null || area(image) >= area(existing.source)){
                    regionCache.put(name, region);
                }
            }
        }
    }

    private void installAtlas(){
        Core.atlas = new TextureAtlas(){
            @Override
            public AtlasRegion find(String name){
                GenRegion region = regionCache.get(name);
                return region != null ? region : missing(name);
            }

            @Override
            public TextureRegion find(String name, String def){
                return find(name, find(def));
            }

            @Override
            public TextureRegion find(String name, TextureRegion def){
                return has(name) ? regionCache.get(name) : def;
            }

            @Override
            public boolean has(String s){
                return regionCache.containsKey(s);
            }

            {
                error = find("error");
            }
        };
    }

    private GenRegion missing(String name){
        GenRegion region = new GenRegion();
        region.name = name;
        region.context = this;
        region.invalid = true;
        region.source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        region.set(0, 0, 1, 1);
        return region;
    }

    private int area(BufferedImage image){
        return image.getWidth() * image.getHeight();
    }

    public void generate(String name, Runnable run){
        Timers.mark();
        run.run();
        Log.info("&ly[Generator]&lc Time to generate &lm" + name + "&lc: &lg" + Timers.elapsed() + "&lcms");
    }

    public Image create(int width, int height){
        return new Image(this, width, height);
    }

    public Image get(String name){
        return get(Core.atlas.find(name));
    }

    public Image get(TextureRegion region){
        GenRegion.validate(region);

        return new Image(this, region);
    }

    public void addGenerated(String name, BufferedImage image){
        GenRegion region = new GenRegion();
        region.name = name;
        region.context = this;
        region.source = image;
        region.set(0, 0, image.getWidth(), image.getHeight());
        regionCache.put(name, region);
    }

    public void packOutput() throws IOException{
        outputRoot.mkdirs();

        Seq<GenRegion> regions = new Seq<>();
        for(GenRegion region : regionCache.values()){
            if(region.source != null){
                regions.add(region);
            }
        }

        regions.sort((a, b) -> {
            int left = Math.max(b.width, b.height);
            int right = Math.max(a.width, a.height);
            return Integer.compare(left, right);
        });

        PixmapPacker packer = new PixmapPacker(1024, 512, 2, true, false, false, new PixmapPacker.GuillotineStrategy());
        packer.setAllowMultiplePages(true);

        for(GenRegion region : regions){
            Pixmap pixmap = toPixmap(region.source);
            try{
                packer.pack(region.name, pixmap);
            }finally{
                pixmap.dispose();
            }
        }

        try{
            writePackedAtlas(packer);
        }finally{
            packer.dispose();
        }
    }

    private void writePackedAtlas(PixmapPacker packer) throws IOException{
        StringBuilder atlas = new StringBuilder();

        for(int pageIndex = 0; pageIndex < packer.getPages().size; pageIndex++){
            PixmapPacker.Page page = packer.getPages().get(pageIndex);
            String pageName = pageIndex == 0 ? "sprites.png" : "sprites" + pageIndex + ".png";
            Fi pageFile = outputRoot.child(pageName);

            PixmapIO.writePng(pageFile, page.getPixmap());

            atlas.append(pageName).append('\n');
            atlas.append("size: ").append(page.getPixmap().width).append(",").append(page.getPixmap().height).append('\n');
            atlas.append("format: RGBA8888\n");
            atlas.append("filter: Nearest,Nearest\n");
            atlas.append("repeat: none\n");

            for(String name : page.getRects().keys()){
                PixmapPacker.PixmapPackerRect rect = page.getRects().get(name);
                atlas.append(name).append('\n');
                atlas.append("  rotate: false\n");
                atlas.append("  xy: ").append((int)rect.x).append(", ").append((int)rect.y).append('\n');
                atlas.append("  size: ").append((int)rect.width).append(", ").append((int)rect.height).append('\n');
                atlas.append("  orig: ").append(rect.originalWidth).append(", ").append(rect.originalHeight).append('\n');
                atlas.append("  offset: ").append(rect.offsetX).append(", ").append((int)(rect.originalHeight - rect.height - rect.offsetY)).append('\n');
                if(rect.splits != null){
                    atlas.append("  split: ").append(rect.splits[0]).append(", ").append(rect.splits[1]).append(", ").append(rect.splits[2]).append(", ").append(rect.splits[3]).append('\n');
                }
                if(rect.pads != null){
                    atlas.append("  pad: ").append(rect.pads[0]).append(", ").append(rect.pads[1]).append(", ").append(rect.pads[2]).append(", ").append(rect.pads[3]).append('\n');
                }
                atlas.append("  index: -1\n");
            }

            if(pageIndex < packer.getPages().size - 1){
                atlas.append('\n');
            }
        }

        try(Writer writer = new BufferedWriter(outputRoot.child("sprites.atlas").writer(false))){
            writer.write(atlas.toString());
        }
    }

    private Pixmap toPixmap(BufferedImage image){
        Pixmap pixmap = new Pixmap(image.getWidth(), image.getHeight());
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        for(int y = 0; y < image.getHeight(); y++){
            int row = y * image.getWidth();
            for(int x = 0; x < image.getWidth(); x++){
                int argb = pixels[row + x];
                int rgba = (argb << 8) | ((argb >>> 24) & 0xff);
                pixmap.setRaw(x, y, rgba);
            }
        }
        return pixmap;
    }

    public void err(String message, Object... args){
        Log.err(message, args);
        System.exit(-1);
    }
}
