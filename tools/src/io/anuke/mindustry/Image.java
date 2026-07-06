package io.anuke.mindustry;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Structs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Image {
    private static ArrayList<Image> toDispose = new ArrayList<>();

    private final ImageContext context;
    private BufferedImage image;
    private Graphics2D graphics;
    private Color color = new Color();

    public Image(ImageContext context, TextureRegion region){
        this(context, region.width, region.height);

        draw(region);
    }

    public Image(ImageContext context, int width, int height){
        this.context = context;

        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.graphics = image.createGraphics();

        toDispose.add(this);
    }

    public int width(){
        return image.getWidth();
    }

    public int height(){
        return image.getHeight();
    }

    public boolean isEmpty(int x, int y){
        if(!Structs.inBounds(x, y, width(), height())){
            return true;
        }
        Color color = getColor(x, y);
        return color.a <= 0.001f;
    }

    public Color getColor(int x, int y){
        int i = image.getRGB(x, y);
        color.argb8888(i);
        return color;
    }

    public void draw(int x, int y, Color color){
        graphics.setColor(new java.awt.Color(color.r, color.g, color.b, color.a));
        graphics.fillRect(x, y, 1, 1);
    }

    /**Draws a region at the top left corner.*/
    public void draw(TextureRegion region){
        draw(region, 0, 0, false, false);
    }

    /**Draws a region at the center.*/
    public void drawCenter(TextureRegion region){
        draw(region, (width() - region.width)/2, (height() - region.height)/2, false, false);
    }

    /**Draws a region at the center.*/
    public void drawCenter(TextureRegion region, boolean flipx, boolean flipy){
        draw(region, (width() - region.width)/2, (height() - region.height)/2, flipx, flipy);
    }

    /**Draws an image at the top left corner.*/
    public void draw(Image image){
        draw(image, 0, 0);
    }

    /**Draws an image at the coordinates specified.*/
    public void draw(Image image, int x, int y){
        graphics.drawImage(image.image, x, y, null);
    }

    public void draw(TextureRegion region, boolean flipx, boolean flipy){
        draw(region, 0, 0, flipx, flipy);
    }

    public void draw(TextureRegion region, int x, int y, boolean flipx, boolean flipy){
        GenRegion.validate(region);

        int width = region.width;
        int height = region.height;

        int leftTrim = Math.max(0, -x);
        int topTrim = Math.max(0, -y);
        int rightTrim = Math.max(0, x + width - width());
        int bottomTrim = Math.max(0, y + height - height());

        int drawWidth = width - leftTrim - rightTrim;
        int drawHeight = height - topTrim - bottomTrim;

        if(drawWidth <= 0 || drawHeight <= 0){
            return;
        }

        int dstX1 = x + leftTrim;
        int dstY1 = y + topTrim;
        int dstX2 = dstX1 + drawWidth;
        int dstY2 = dstY1 + drawHeight;

        int srcX1 = flipx ? region.getX() + width - leftTrim : region.getX() + leftTrim;
        int srcX2 = flipx ? region.getX() + rightTrim : region.getX() + width - rightTrim;
        int srcY1 = flipy ? region.getY() + height - topTrim : region.getY() + topTrim;
        int srcY2 = flipy ? region.getY() + bottomTrim : region.getY() + height - bottomTrim;

        graphics.drawImage(((GenRegion)region).source, dstX1, dstY1, dstX2, dstY2, srcX1, srcY1, srcX2, srcY2, null);
    }

    /** @param name Name of texture file name to create, without any extensions.*/
    public void save(String name){
        context.addGenerated(name, image);
    }

    public static int total(){
        return toDispose.size();
    }

    public static void dispose(){
        for(Image image : toDispose){
            image.graphics.dispose();
        }
        toDispose.clear();
    }
}
