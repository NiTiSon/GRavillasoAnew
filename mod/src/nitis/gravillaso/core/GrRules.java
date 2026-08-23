package nitis.gravillaso.core;

import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.game.*;
import mindustry.io.*;

public class GrRules {
    /** Whenever the frost mechanic is enabled */
    public boolean frostEnabled = false;
    /** Base environment temperature */
    public float baseTemperature = -0.3f;
    /** Team-specific rules. */
    public TeamRules teams = new TeamRules();

    public static GrRules getFrom(Rules rules) {
        return rules.tags.get("gr-rules") == null
                ? new GrRules()
                : JsonIO.read(GrRules.class, rules.tags.get("gr-rules"));
    }

    public void appendTo(Rules rules) {
        rules.tags.put("gr-rules", JsonIO.write(this));
    }

    /** A team-specific ruleset. */
    public static class TeamRule {
        /** How much frost damage block takes per hit */
        public float blockFrostDamageMultiplier = 1.0f;
        /** How much frost damage unit takes per hit */
        public float unitFrostDamageMultiplier = 1.0f;
        /** Damage multiplier to conduit-like blocks containing water or brine */
        public float conduitCrackingDamageMultiplier = 1.0f;

        public TeamRule(){
        }

        public TeamRule(Team team){
            if(team == Team.derelict){
                blockFrostDamageMultiplier = 0f;
            }
        }
    }

    /** A simple map for storing TeamRules in an efficient way without hashing. */
    public static class TeamRules implements JsonSerializable {
        final TeamRule[] values = new TeamRule[Team.all.length];

        public TeamRule get(Team team){
            TeamRule out = values[team.id];
            return out == null ? (values[team.id] = new TeamRule(team)) : out;
        }

        @Override
        public void write(Json json){
            for(Team team : Team.all){
                if(values[team.id] != null){
                    json.writeValue(team.id + "", values[team.id], TeamRule.class);
                }
            }
        }

        @Override
        public void read(Json json, JsonValue jsonData){
            for(JsonValue value : jsonData){
                values[Integer.parseInt(value.name)] = json.readValue(TeamRule.class, value);
            }
        }
    }
}