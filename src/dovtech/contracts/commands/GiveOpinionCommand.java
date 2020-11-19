package dovtech.contracts.commands;

import api.common.GameServer;
import api.faction.StarFaction;
import api.mod.StarLoader;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.Contracts;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
import dovtech.contracts.util.FactionUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;

import java.util.Collection;

public class GiveOpinionCommand extends ChatCommand {

    public GiveOpinionCommand() {
        super("give_opinion", "/give_opinion <from_faction> [to_faction]", "Changes the specified faction's opinion score of another faction to be max. Separate arguments using commas if needed.", true, Contracts.getInstance());
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if (args.length == 2) {
            String fromFactionName = args[0];
            String toFactionName = args[1];
            Collection<Faction> factions = GameServer.getServerState().getFactionManager().getFactionCollection();
            StarFaction fromFaction = null;
            StarFaction toFaction = null;
            if (fromFactionName.equals(toFactionName)) {
                PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + fromFactionName + " is the same as " + toFactionName + "!");
                return true;
            }
            for (Faction f : factions) {
                if (f.getName().equals(fromFactionName)) {
                    fromFaction = new StarFaction(f);
                } else if (f.getName().equals(toFactionName)) {
                    toFaction = new StarFaction(f);
                }
            }

            if (fromFaction == null) {
                PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + fromFactionName + " does not exist!");
                return true;
            }
            if (toFaction == null) {
                PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + toFactionName + " does not exist!");
                return true;
            }
            FactionUtils.setOpinionAdmin(fromFaction, toFaction);
            PlayerUtils.sendMessage(sender, "Faction " + fromFaction.getName() + " now has maximum opinion of " + toFaction.getName() + ".");
            return true;
        } else if (args.length == 1) {
            String fromFactionName = args[0];
            StarFaction fromFaction = null;
            StarFaction toFaction = null;
            if (sender.getFactionId() == 0) {
                PlayerUtils.sendMessage(sender, "[ERROR]: You must be in a faction or specify one in arguments to use this command!");
            } else {
                toFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(sender.getFactionId()));
                Collection<Faction> factions = GameServer.getServerState().getFactionManager().getFactionCollection();
                for (Faction faction : factions) {
                    if (faction.getName().equals(fromFactionName)) {
                        fromFaction = new StarFaction(faction);
                    }
                }
                if (fromFaction == null) {
                    PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + fromFactionName + " does not exist!");
                } else {
                    FactionUtils.setOpinionAdmin(fromFaction, toFaction);
                    PlayerUtils.sendMessage(sender, "Faction " + fromFaction.getName() + " now has maximum opinion of " + toFaction.getName() + ".");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
