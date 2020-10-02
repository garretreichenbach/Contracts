package dovtech.contracts.commands;

import api.common.GameServer;
import api.faction.StarFaction;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import java.util.Arrays;
import java.util.Collection;

public class SetOpinionCommand extends ChatCommand {

    public SetOpinionCommand() {
        super("setOpinion", "/setOpinion <opinionScore> <factionName> [playerName]", "Changes the specified faction's opinion score of a player. Separate arguments using commas if needed.", true);
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        try {
            if (args != null && args.length >= 2) {
                String[] trueArgs;
                if (Arrays.toString(args).contains(",")) {
                    trueArgs = Arrays.toString(args).split(", ");
                } else {
                    trueArgs = args;
                }
                if (args.length == 3) {
                    int newOpinion = Integer.parseInt(trueArgs[0]);
                    String factionName = trueArgs[1];
                    Collection<Faction> factions = GameServer.getServerState().getFactionManager().getFactionCollection();
                    for(Faction f : factions) {
                        if(f.getName().equals(factionName)) {
                            StarFaction faction = new StarFaction(f);
                            PlayerData playerData = DataUtils.getPlayerData(trueArgs[2]);
                            if(playerData != null) {
                                playerData.setOpinionScore(faction, newOpinion);
                                DataUtils.addPlayer(playerData);
                                return true;
                            } else {
                                PlayerUtils.sendMessage(sender, "[ERROR]: Player " + trueArgs[2] + " does not exist!");
                                return true;
                            }
                        }
                    }
                    PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + factionName + " does not exist!");
                    return true;
                } else if (args.length == 2) {
                    int newOpinion = Integer.parseInt(trueArgs[0]);
                    String factionName = trueArgs[1];
                    Collection<Faction> factions = GameServer.getServerState().getFactionManager().getFactionCollection();
                    for(Faction f : factions) {
                        if(f.getName().equals(factionName)) {
                            StarFaction faction = new StarFaction(f);
                            PlayerData playerData = DataUtils.getPlayerData(sender.getName());
                            playerData.setOpinionScore(faction, newOpinion);
                            DataUtils.addPlayer(playerData);
                            return true;
                        }
                    }
                    PlayerUtils.sendMessage(sender, "[ERROR]: Faction " + factionName + " does not exist!");
                    return true;

                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
