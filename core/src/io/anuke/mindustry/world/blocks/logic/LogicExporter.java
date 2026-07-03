package io.anuke.mindustry.world.blocks.logic;

import arc.struct.IntSeq;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.modules.ItemModule;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import io.anuke.mindustry.graphics.Palette;
import arc.scene.ui.layout.Table;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.tilesize;

public class LogicExporter extends LogicBlock {
    public UnitType droneType;
    public int maxDrones = 2;

    public LogicExporter(String name) {
        super(name);
        hasItems = true;
        itemCapacity = 100;
    }

    @Override
    public void update(Tile tile) {
        LogicExporterEntity entity = tile.entity();

        if (entity.droneIDs.size < maxDrones && (entity.spawnTimer += Timers.delta()) >= 60f * 5) {
            BaseUnit unit = droneType.create(tile.getTeam());
            unit.setSpawner(tile);
            unit.set(tile.worldx(), tile.worldy());
            unit.add();
            entity.droneIDs.add(unit.id);
            entity.spawnTimer = 0;
        }

        if (entity.targetPos == -1) {//automatic mode buh
            if ((entity.findTimer += Timers.delta()) >= 60f) {
                entity.findTimer = 0;
                entity.targetImporter = findBestImporter(tile);
            }
        } else {
            Tile target = Vars.world.tile(entity.targetPos);
            if (target != null && target.entity() instanceof LogicImporter.LogicImporterEntity) {
                entity.targetImporter = (LogicImporter.LogicImporterEntity) target.entity();
            } else {
                entity.targetImporter = null;
            }
        }
    }

    private LogicImporter.LogicImporterEntity findBestImporter(Tile tile) {
        LogicExporterEntity entity = tile.entity();
        LogicImporter.LogicImporterEntity best = null;
        int minItems = Integer.MAX_VALUE;

        for (int x = 0; x < Vars.world.width(); x++) {//TODO refactor this, really unoptimized
            for (int y = 0; y < Vars.world.height(); y++) {
                Tile other = Vars.world.tile(x, y);
                if (other != null && other.entity() instanceof LogicImporter.LogicImporterEntity) {
                    LogicImporter.LogicImporterEntity importer = (LogicImporter.LogicImporterEntity) other.entity();
                    if (importer.selectedItem != null && entity.items.get(importer.selectedItem) > 0) {
                        int totalItems = importer.items.total();
                        if (totalItems < minItems) {
                            minItems = totalItems;
                            best = importer;
                        }
                    }
                }
            }
        }
        return best;
    }

    @Override
    public void unitRemoved(Tile tile, Unit unit) {
        LogicExporterEntity entity = tile.entity();
        entity.droneIDs.remove(unit.id);
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source) {
        LogicExporterEntity entity = tile.entity();
        return entity.items.total() < itemCapacity;
    }

    @Override
    public void drawConfigure(Tile tile) {
        super.drawConfigure(tile);
        float sin = Mathf.absin(Timers.time(), 6f, 1f);
        LogicExporterEntity entity = tile.entity();

        if (entity.targetPos != -1) {
            Tile target = Vars.world.tile(entity.targetPos);
            if (target != null && target.block() instanceof LogicImporter) {
                Draw.color(Palette.place);
                Lines.stroke(1.5f);
                Lines.poly(target.drawx(), target.drawy(), 20, (target.block().size) * tilesize + sin);
                Draw.reset();
            }
        }
    }

    @Override
    public void buildTable(Tile tile, Table table) {
        LogicExporterEntity entity = tile.entity();
        table.add("Logic Exporter").row();//more uselesss text bc yes
        
        table.addImageButton("icon-cancel", "clear-toggle", 24, () -> {
            setLogicTarget(null, tile, -1);
            entity.targetImporter = null;
        }).size(38).pad(1).get().setChecked(entity.targetPos == -1);
        
        table.add("Auto-link").left();
    }

    @Override
    public TileEntity newEntity() {
        return new LogicExporterEntity();
    }

    public static class LogicExporterEntity extends LogicEntity {
        public IntSeq droneIDs = new IntSeq();
        public float spawnTimer;
        public float findTimer;
        public LogicImporter.LogicImporterEntity targetImporter;
    }
}


