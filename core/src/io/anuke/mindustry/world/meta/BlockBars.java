package io.anuke.mindustry.world.meta;

import arc.struct.Seq;
import io.anuke.mindustry.world.BarType;

public class BlockBars{
    private Seq<BlockBar> list = Seq.with(new BlockBar(BarType.health, false, tile -> tile.entity.health / (float) tile.block().health));

    public void add(BlockBar bar){
        list.add(bar);
    }

    public void replace(BlockBar bar){
        remove(bar.type);
        list.add(bar);
    }

    public void remove(BarType type){
        for(BlockBar bar : list){
            if(bar.type == type){
                list.remove(bar, true);
                break;
            }
        }
    }

    public void removeAll(BarType type){
        Seq<BlockBar> removals = new Seq<>(4);

        for(BlockBar bar : list){
            if(bar.type == type){
                removals.add(bar);
            }
        }

        list.removeAll(removals, true);
    }

    public Seq<BlockBar> list(){
        return list;
    }
}

