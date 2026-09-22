package nitis.gravillaso.core;

import arc.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;

public class GrObjectives{
    public static class Blocked implements Objective{
        @Override
        public boolean complete(){
            return false;
        }

        @Override
        public String display(){
            return Core.bundle.get("requirement.blocked");
        }
    }

    public static class OnWave implements Objective{
        public SectorPreset preset;
        public int wave;

        public OnWave(SectorPreset zone, int wave){
            this.preset = zone;
            this.wave = wave;
        }

        @Override
        public boolean complete(){
            return preset.sector.hasBase() && preset.sector.info.wave >= wave;
        }

        @Override
        public String display(){
            return Core.bundle.format("requirement.onwave", preset.localizedName, wave);
        }

        @Override
        public String toString(){
            return "onSector: " + preset + ", wave " + wave;
        }
    }
}
