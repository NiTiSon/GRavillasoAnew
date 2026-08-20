package nitis.gravillaso.core;

import arc.math.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.ui.dialogs.*;
import nitis.gravillaso.world.temperature.*;

import static mindustry.Vars.ui;
import static nitis.gravillaso.GravillasoMod.grState;

public class GrUI{
    /** Injects new menus into vanilla UI */
    public static void inject(){
        ClientLauncher.runOnClientLoad(() -> {
            ui.settings.dev.checkPref("gr-showtemperature", false, (value) -> TemperatureSystem.enableDebugView = value);

            injectCustomRules();
        });
    }

    private static void injectCustomRules(){
        // for some reason `additionalSetup` field is not static, so I've tracked all CustomRulesDialog instances
        // FIX: I've already fixed that, but need time to wait until commit goes into the stable version
        // FIX: https://github.com/Anuken/Mindustry/pull/12478
        CustomRulesDialog play = Reflect.get(MapPlayDialog.class, Reflect.get(ui.custom, "dialog"), "dialog");
        CustomRulesDialog editor = Reflect.get(MapInfoDialog.class, Reflect.get(ui.editor, "infoDialog"), "ruleInfo");
        CustomRulesDialog paused = Reflect.get(PausedDialog.class, ui.paused, "rulesDialog");

        for(CustomRulesDialog dialog : Seq.with(play, editor, paused)){
            dialog.additionalSetup.add(() -> {
                Rules rules = Reflect.get(dialog, "rules");
                GrRules gr = GrRules.getFrom(rules);

                Runnable save = () -> gr.appendTo(rules);

                int env = dialog.categoryNames.indexOf("environment");
                if(env >= 0 && env < dialog.categories.size){
                    Table prev = dialog.current;
                    dialog.current = dialog.categories.get(env);
                    dialog.check("@rules.gr.colddisabled", b -> { gr.coldEnabled = !b; save.run(); }, () -> !gr.coldEnabled);
                    dialog.current.table(t -> {
                        t.left();
                        t.add("@rules.gr.basetemperature").left().padRight(5);
                        t.field(Strings.autoFixed(gr.baseTemperature, 2), s -> { gr.baseTemperature = Mathf.clamp(Strings.parseFloat(s), -1f, 1f); save.run(); })
                        .valid(Strings::canParseFloat).padRight(50f).width(120f).left();
                    }).padTop(0).row();
                    dialog.current = prev;
                }

                // team-specific: multipliers into the vanilla per-team collapsers
                int teams = dialog.categoryNames.indexOf("teams");
                if(teams >= 0 && teams < dialog.categories.size){
                    Seq<Collapser> collapsers = new Seq<>();
                    for(Element e : dialog.categories.get(teams).getChildren()){
                        if(e instanceof Table t){
                            for(Element c : t.getChildren()){
                                if(c instanceof Collapser col) collapsers.add(col);
                            }
                        }
                    }
                    // collapsers only line up with Team.baseTeams when no search filter dropped any
                    if(collapsers.size == Team.baseTeams.length){
                        Table prev = dialog.current;
                        for(int i = 0; i < collapsers.size; i++){
                            GrRules.TeamRule tr = gr.teams.get(Team.baseTeams[i]);
                            dialog.current = (Table)collapsers.get(i).getChildren().first();
                            dialog.number("@rules.gr.blockcolddamage", f -> { tr.blockColdDamageMultiplier = f; save.run(); }, () -> tr.blockColdDamageMultiplier);
                            dialog.number("@rules.gr.unitcolddamage", f -> { tr.unitColdDamageMultiplier = f; save.run(); }, () -> tr.unitColdDamageMultiplier);
                            dialog.number("@rules.gr.conduitcracking", f -> { tr.conduitCrackingDamageMultiplier = f; save.run(); }, () -> tr.conduitCrackingDamageMultiplier);
                        }
                        dialog.current = prev;
                    }
                }
            });
        }

        // All next lines is related to the setting the grState.rules field
        // There are no proper way to handle rule update (for now v159)

        // copied from PausedDialog.java

        paused.hidden(() -> {
            Rules rules = Reflect.get(paused, "rules");
            grState.rules = GrRules.getFrom(rules);
            Log.info("Pause editor saved gr rules");
        });
    }
}
