package nitis.gravillaso.core;

import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import arc.util.serialization.Json.JsonSerializable;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.io.JsonIO;

public class GRRules {
    /** Whenever the cold mechanic is enabled */
    public boolean coldEnabled = true;
    /** Base environment temperature */
    public float baseTemperature = 0.0f;
    /** Team-specific rules. */
    public TeamRules teams = new TeamRules();

    public static GRRules getFrom(Rules rules) {
        return rules.tags.get("gr-rules") == null
                ? new GRRules()
                : JsonIO.read(GRRules.class, rules.tags.get("gr-rules"));
    }

    public void appendTo(Rules rules) {
        rules.tags.put("gr-rules", JsonIO.write(this));
    }

    /** A team-specific ruleset. */
    public static class TeamRule {
        /** How much cold damage block takes per hit */
        public float blockColdDamageMultiplier = 1.0f;
        /** How much cold damage unit takes per hit */
        public float unitColdDamageMultiplier = 1.0f;
        /** Damage multiplier to conduit-like blocks containing water or brine */
        public float conduitCrackingDamageMultiplier = 1.0f;

        public TeamRule(){
        }

        public TeamRule(Team team){
            if(team == Team.derelict){
                blockColdDamageMultiplier = 0.25f;
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
