package nitis.gravillaso.ui.dialog;

import arc.scene.Element;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.editor.MapInfoDialog;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.MapPlayDialog;
import mindustry.ui.dialogs.PausedDialog;
import nitis.gravillaso.GravillasoMod;
import nitis.gravillaso.core.GRRules;

import static mindustry.Vars.state;
import static mindustry.Vars.ui;
import static nitis.gravillaso.GravillasoMod.grState;

public final class CustomRulesDialogExtension {
    public static void inject() {
        CustomRulesDialog play = Reflect.get(MapPlayDialog.class, Reflect.get(ui.custom, "dialog"), "dialog");
        CustomRulesDialog editor = Reflect.get(MapInfoDialog.class, Reflect.get(ui.editor, "infoDialog"), "ruleInfo");
        CustomRulesDialog paused = Reflect.get(PausedDialog.class, ui.paused, "rulesDialog");

        for(CustomRulesDialog dialog : Seq.with(play, editor, paused)){
            dialog.additionalSetup.add(() -> {
                Rules rules = Reflect.get(dialog, "rules");
                GRRules gr = GRRules.getFrom(rules);

                Runnable save = () -> gr.appendTo(rules);

                int env = dialog.categoryNames.indexOf("environment");
                if(env >= 0 && env < dialog.categories.size){
                    Table prev = dialog.current;
                    dialog.current = dialog.categories.get(env);
                    dialog.check("@rules.gr.colddisabled", b -> { gr.coldEnabled = !b; save.run(); }, () -> !gr.coldEnabled);
                    dialog.number("@rules.gr.basetemperature", f -> { gr.baseTemperature = f; save.run(); }, () -> gr.baseTemperature, -1f, 1f);
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
                            GRRules.TeamRule tr = gr.teams.get(Team.baseTeams[i]);
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
            grState.rules = GRRules.getFrom(rules);
            Log.info("Pause editor saved gr rules");
        });
    }
}
