package io.anuke.mindustry.world.modules;

import com.badlogic.gdx.utils.LongArray;
import io.anuke.mindustry.world.blocks.power.PowerGraph;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PowerModule extends BlockModule{
    public float amount;
    public PowerGraph graph = new PowerGraph();
    public LongArray links = new LongArray();

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeFloat(amount);

        stream.writeShort(links.size);
        for(int i = 0; i < links.size; i++){
            stream.writeLong(links.get(i));
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        amount = stream.readFloat();
        if(Float.isNaN(amount)){
            amount = 0f;
        }
        if(amount < 0f){
            amount = 0f;
        }

        short amount = stream.readShort();
        for(int i = 0; i < amount; i++){
            links.add(stream.readLong());
        }
    }
}
