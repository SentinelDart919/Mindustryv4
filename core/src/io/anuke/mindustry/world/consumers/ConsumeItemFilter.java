package io.anuke.mindustry.world.consumers;

import arc.struct.Seq;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.BlockStats;
import io.anuke.mindustry.world.meta.values.ItemFilterValue;
import arc.func.Boolf;
import arc.scene.ui.layout.Table;
import static io.anuke.mindustry.Vars.*;

public class ConsumeItemFilter extends Consume{
    private final Boolf<Item> filter;

    public ConsumeItemFilter(Boolf<Item> item){
        this.filter = item;
    }

    @Override
    public void buildTooltip(Table table){
        Seq<Item> list = new Seq<>();

        for(Item item : content.items()){
            if(filter.get(item)) list.add(item);
        }

        for(int i = 0; i < list.size; i++){
            Item item = list.get(i);
            table.image(item.region).size(8 * 4).padRight(2).padLeft(2);
            if(i != list.size - 1){
                table.add("/");
            }
        }
    }

    @Override
    public String getIcon(){
        return "icon-item";
    }

    @Override
    public void update(Block block, TileEntity entity){

    }

    @Override
    public boolean valid(Block block, TileEntity entity){
        for(int i = 0; i < content.items().size; i++){
            Item item = content.item(i);
            if(entity.items != null && entity.items.has(item) && this.filter.get(item)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void display(BlockStats stats){
        stats.add(optional ? BlockStat.boostItem : BlockStat.inputItem, new ItemFilterValue(filter));
    }
}
