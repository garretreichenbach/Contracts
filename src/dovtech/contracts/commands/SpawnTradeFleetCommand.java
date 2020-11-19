package dovtech.contracts.commands;

import api.common.GameServer;
import api.element.block.Blocks;
import api.entity.Fleet;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.Contracts;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.simulation.npc.NPCFaction;

public class SpawnTradeFleetCommand extends ChatCommand {

    public SpawnTradeFleetCommand() {
        super("spawn_trade_fleet", "/spawn_trade_fleet", "Spawns a trade fleet at your current position.", true, Contracts.getInstance());
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        for (Faction faction : GameServer.getServerState().getFactionManager().getFactionCollection()) {
            if (faction.isNPC() && faction.getName().toLowerCase().contains("trad")) {
                NPCFaction npcFaction = (NPCFaction) faction;
                ElementCountMap elementCountMap = new ElementCountMap();
                elementCountMap.inc(Blocks.CACTUS.getId(), 420);
                Fleet tradeFleet = new Fleet(npcFaction.getFleetManager().spawnTradingFleet(elementCountMap, sender.getCurrentSector(), sender.getCurrentSector()));
                tradeFleet.moveTo(sender.getCurrentSector().x, sender.getCurrentSector().y, sender.getCurrentSector().z);
                PlayerUtils.sendMessage(sender, "Spawned Trade Fleet at " + tradeFleet.getFlagshipSector().getCoordinates().toString());
                return true;
            }
        }
        return true;
    }
}
