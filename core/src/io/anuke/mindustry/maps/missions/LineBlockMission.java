package io.anuke.mindustry.maps.missions;

import arc.math.geom.Bresenham2;
import arc.math.geom.Point2;
import arc.struct.Seq;
import io.anuke.mindustry.world.Block;

public class LineBlockMission extends Mission{
    private Seq<BlockLocMission> points = new Seq<>();
    private int completeIndex;

    public LineBlockMission(Block block, int x1, int y1, int x2, int y2, int rotation){
        Seq<Point2> points = new Bresenham2().line(x1, y1, x2, y2);
        for(Point2 point : points){
            this.points.add(new BlockLocMission(block, point.x, point.y, rotation));
        }
    }

    @Override
    public boolean isComplete(){
        while(completeIndex < points.size && points.get(completeIndex).isComplete()){
            completeIndex ++;
        }
        return completeIndex >= points.size;
    }

    @Override
    public void drawOverlay(){
        if(completeIndex < points.size){
            points.get(completeIndex).drawOverlay();
        }
    }

    @Override
    public void reset(){
        completeIndex = 0;
    }

    @Override
    public String displayString(){
        if(completeIndex < points.size){
            return points.get(completeIndex).displayString();
        }
        return points.first().displayString();
    }
}
