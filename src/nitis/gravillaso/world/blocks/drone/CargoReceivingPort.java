package nitis.gravillaso.world.blocks.drone;

import arc.Core;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.entities.TargetPriority;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class CargoReceivingPort extends Block {
    public int maxFilterSize = 4;

    public CargoReceivingPort(String name) {
        super(name);
        priority = TargetPriority.transport;
        configurable = true;
        update = true;
        hasItems = true;
        hasLiquids = true;
        solid = true;
        group = BlockGroup.transportation;
    }

    public class CargoReceivingPortBuilding extends Building {
    }
}
