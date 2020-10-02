package dovtech.contracts.util;

import api.DebugFile;
import api.common.GameServer;
import api.entity.*;
import api.faction.StarFaction;
import api.universe.StarSector;
import api.universe.StarUniverse;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import org.schema.common.util.linAlg.Vector3i;

import java.util.HashMap;
import java.util.Random;

public class ContractUtils {

    public static HashMap<StarPlayer, StarSector> cargoSectors = new HashMap<>();
    public static HashMap<Contract, Long> tradeFleets = new HashMap<>();

    public static StarSector getNearbyRandomSector(StarSector originSector, int range) {
        Random random = new Random();
        return StarUniverse.getUniverse().getSector(originSector.getCoordinates().x + random.nextInt(range), originSector.getCoordinates().y + random.nextInt(range), originSector.getCoordinates().z + random.nextInt(range));
    }

    public static void startContractTimer(final Contract contract, final StarPlayer player) {
        new StarRunnable() {
            @Override
            public void run() {
                if (contract.getTimer() >= Contracts.getInstance().contractTimerMax) {
                    if (contract.getClaimants().contains(player)) {
                        DataUtils.timeoutContract(contract, player);
                    } else {
                        contract.setTimer(0);
                    }
                } else {
                    contract.setTimer(contract.getTimer() + 1);
                }
            }
        }.runTimer(1000);
    }

    private static void triggerFleetRetreat(StarPlayer player, Fleet tradeFleet, Contract contract) {
        if (player != null) {
            PlayerUtils.sendMessage(player.getPlayerState(), "[TRADERS]: This is too dangerous! We must retreat to somewhere safer!");
            StarSector safeSector = getNearbyRandomSector(tradeFleet.getFlagshipSector(), 5);
            tradeFleet.moveTo(safeSector);
            PlayerUtils.sendMessage(player.getPlayerState(), "It appears the trade fleet has abandoned the contract and fled...");
            player.setCredits(player.getCredits() + (contract.getReward() / 5));
            tradeFleet.delete();
            StarSector starSector = StarUniverse.getUniverse().getSector(new Vector3i(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]));
            if (starSector.getInternalSector().getFactionId() != 0) {
                StarFaction faction = new StarFaction(GameServer.getServerState().getFactionManager().getFaction(starSector.getInternalSector().getFactionId()));
                int members = faction.getActiveMembers().size();
                int refund = (contract.getReward() / 2) / members;
                for (StarPlayer factionMember : faction.getActiveMembers()) {
                    factionMember.sendMail(contract.getContractor().getName(), "Trade Cargo Intercepted", "We regret to inform you that one of your faction's recent trade requests had it's cargo intercepted. We apologize for this interruption and have issued a partial refund to your member's accounts.");
                    factionMember.setCredits(factionMember.getCredits() + refund);
                }
            }
            DataUtils.removeContract(contract, true, player);
            contract.setFinished(true);
        } else {

            StarSector starSector = StarUniverse.getUniverse().getSector(new Vector3i(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]));
            StarSector safeSector = getNearbyRandomSector(tradeFleet.getFlagshipSector(), 5);
            tradeFleet.moveTo(safeSector);
            tradeFleet.teleport(safeSector);
            tradeFleet.delete();
            if (starSector.getInternalSector().getFactionId() != 0) {
                StarFaction faction = new StarFaction(GameServer.getServerState().getFactionManager().getFaction(starSector.getInternalSector().getFactionId()));
                int members = faction.getActiveMembers().size();
                int refund = (contract.getReward() / 2) / members;
                for (StarPlayer factionMember : faction.getActiveMembers()) {
                    factionMember.sendMail(contract.getContractor().getName(), "Trade Cargo Intercepted", "We regret to inform you that one of your faction's recent trade requests had it's cargo intercepted. We apologize for this interruption and have issued a partial refund to your member's accounts.");
                    factionMember.setCredits(factionMember.getCredits() + refund);
                }
            }
            DataUtils.removeContract(contract, true);
            contract.setFinished(true);
        }
    }

    public static void startCargoClaimTimer(final Contract contract) {
        new StarRunnable() {
            @Override
            public void run() {
                if (contract.getTimer() < Contracts.getInstance().contractTimerMax) {
                    if (contract.getClaimants().size() == 0) {
                        contract.setTimer(contract.getTimer() + 1);
                        DebugFile.log("TICK TOCK MOTHERFUCKER");
                        DebugFile.log(String.valueOf(tradeFleets.get(contract).intValue()));
                        /*
                        Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(tradeFleets.get(contract)));
                        tradeFleet.getInternalFleet().removeCurrentMoveTarget();
                         */
                    }
                } else {
                    Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(tradeFleets.get(contract)));
                    tradeFleet.moveTo(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]);
                    DataUtils.removeContract(contract, false);
                }
            }
        }.runTimer(1000);
    }

    public static void startCargoTimer(final Contract contract, final StarPlayer player, final StarSector sector) {
        cargoSectors.put(player, sector);
        new StarRunnable() {
            @Override
            public void run() {
                if (contract.getTimer() >= Contracts.getInstance().contractTimerMax) {
                    DataUtils.timeoutContract(contract, player);
                    Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(tradeFleets.get(contract)));
                    tradeFleet.moveTo(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]);
                    cargoSectors.remove(player);
                    DataUtils.timeoutContract(contract, player);
                } else {
                    contract.setTimer(contract.getTimer() + 1);
                }
            }
        }.runTimer(1000);
    }

    public static void startCargoContract(final Contract contract, final StarPlayer player) {
        final Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(tradeFleets.get(contract)));
        tradeFleet.moveTo(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]);
        while (!contract.isFinished()) {
            if (player != null) {
                if (player.getCurrentEntity() != null && player.getCurrentEntity().getSector().getCoordinates().equals(tradeFleet.getInternalFleet().getFlagShip().getSector())) {
                    int damaged = 0;
                    boolean intercepted = false;
                    for (Ship ship : tradeFleet.getMembers()) {
                        if (ship.getCurrentReactor().getHpPercent() <= 99.0) {
                            for (StarEntity entity : ship.getSector().getEntities()) {
                                if ((entity.getAttachedPlayers().size() > 0 && (ship.getFaction().getPersonalEnemies().contains(entity.getPilot()) || player.getFaction().getPersonalEnemies().contains(entity.getPilot()))) || entity.getFaction().getID() == -1 || entity.getFaction().getEnemies().contains(ship.getFaction()) || entity.getFaction().getEnemies().contains(player.getFaction())) {
                                    PlayerUtils.sendMessage(player.getPlayerState(), "[TRADERS]: We're being intercepted! Protect us!");
                                    tradeFleet.defend(tradeFleet.getFlagshipSector());
                                    intercepted = true;
                                    break;
                                }
                            }
                            if (intercepted) break;
                        }

                        if (ship.getCurrentReactor().getHpPercent() <= 70.0) {
                            damaged++;
                        }
                    }

                    if (intercepted) {
                        new StarRunnable() {
                            @Override
                            public void run() {
                                for (StarEntity entity : tradeFleet.getFlagshipSector().getEntities()) {
                                    if ((entity.getAttachedPlayers().size() > 0 && (tradeFleet.getFlagship().getFaction().getPersonalEnemies().contains(entity.getPilot()) || player.getFaction().getPersonalEnemies().contains(entity.getPilot()))) || entity.getFaction().getID() == -1 || entity.getFaction().getEnemies().contains(tradeFleet.getFlagship().getFaction()) || entity.getFaction().getEnemies().contains(player.getFaction())) {
                                        PlayerUtils.sendMessage(player.getPlayerState(), "[TRADERS]: Alright, we're clear. Continuing to " + contract.getTarget().getLocation()[0] + ", " + contract.getTarget().getLocation()[1] + ", " + contract.getTarget().getLocation()[2] + ".");
                                        startCargoContract(contract, player);
                                        return;
                                    }
                                }
                            }
                        }.runTimer(1000);
                    }

                    if (damaged >= 3) {
                        triggerFleetRetreat(player, tradeFleet, contract);
                    }
                } else {
                    new StarRunnable() {
                        @Override
                        public void run() {
                            if (player.getCurrentEntity() != null && !player.getCurrentEntity().getSector().getCoordinates().equals(tradeFleet.getInternalFleet().getFlagShip().getSector())) {
                                contract.setFinished(true);
                                DataUtils.timeoutContract(contract, player);
                            }
                        }
                    }.runTimer(3000);
                }
            } else {
                int damaged = 0;
                for (Ship ship : tradeFleet.getMembers()) {
                    if (ship.getCurrentReactor().getHpPercent() <= 80.0) {
                        damaged++;
                    }
                }
                if (tradeFleet.getMembers().size() / 4 <= damaged) {
                    triggerFleetRetreat(null, tradeFleet, contract);
                }
            }
        }
    }
}
