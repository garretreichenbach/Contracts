package dovtech.contracts.util;

import api.DebugFile;
import api.entity.*;
import api.faction.StarFaction;
import api.universe.StarSector;
import api.universe.StarUniverse;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import api.utils.game.inventory.ItemStack;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.ContractTarget;
import dovtech.contracts.contracts.target.ProductionTarget;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ContractUtils {

    public static HashMap<StarPlayer, StarSector> cargoSectors = new HashMap<>();
    public static HashMap<Contract, Long> tradeFleets = new HashMap<>();

    public static ArrayList<ElementInformation> getResourcesFilter() {
        ArrayList<ElementInformation> filter = new ArrayList<>();
        for (ElementInformation info : ElementKeyMap.getInfoArray()) {
            try {
                if (info != null && !info.isDeprecated() && (info.name.toLowerCase().contains("raw") || info.name.toLowerCase().contains("capsule"))) {
                    filter.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filter;
    }

    public static ArrayList<ElementInformation> getProductionFilter() {
        ArrayList<ElementInformation> filter = new ArrayList<>();
        for (ElementInformation info : ElementKeyMap.getInfoArray()) {
            try {
                if (info != null && info.isInRecipe() && !info.name.toLowerCase().contains("capsule") && !info.name.toLowerCase().contains("raw") && !info.isDeprecated()) {
                    filter.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filter;
    }

    public static void generateRandomContract() {
        Random random = new Random();
        int contractTypeInt = random.nextInt(2 - 1) + 1;
        Contract.ContractType contractType = null;
        ArrayList<Short> possibleIDs = new ArrayList<>();
        String contractName = "";

        int amountInt = random.nextInt(3000 - 100) + 100;
        int basePrice = 0;
        ContractTarget target = null;
        switch(contractTypeInt) {
            case 1:
                contractType = Contract.ContractType.PRODUCTION;
                for(ElementInformation info : getProductionFilter()) possibleIDs.add(info.getId());
                int productionIndex = random.nextInt(possibleIDs.size() - 1) + 1;
                short productionID = possibleIDs.get(productionIndex);
                contractName = "Produce x" + amountInt + " " + ElementKeyMap.getInfo(productionID).getName();
                target = new ProductionTarget();
                ItemStack productionStack = new ItemStack(productionID);
                productionStack.setAmount(amountInt);
                basePrice = (int) ElementKeyMap.getInfo(productionID).getPrice(true);
                ItemStack[] productionStacks = new ItemStack[] {productionStack};
                target.setTargets(productionStacks);
                break;
            case 2:
                contractType = Contract.ContractType.MINING;
                for(ElementInformation info : getResourcesFilter()) possibleIDs.add(info.getId());
                int miningIndex = random.nextInt(possibleIDs.size() - 1) + 1;
                short miningID = possibleIDs.get(miningIndex);
                contractName = "Produce x" + amountInt + " " + ElementKeyMap.getInfo(miningID).getName();
                target = new ProductionTarget();
                ItemStack miningStack = new ItemStack(miningID);
                miningStack.setAmount(amountInt);
                basePrice = (int) ElementKeyMap.getInfo(miningID).getPrice(true);
                ItemStack[] miningStacks = new ItemStack[] {miningStack};
                target.setTargets(miningStacks);
                break;
        }
        int reward = (int) ((basePrice * amountInt) * 1.3);

        Contract randomContract = new Contract(Contracts.getInstance().tradersFactionID, contractName, contractType, reward, UUID.randomUUID().toString(), target);
        DataUtils.addContract(randomContract);
    }

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
                    cancel();
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
                StarFaction faction = StarFaction.fromId(starSector.getInternalSector().getFactionId());
                int members = faction.getActiveMembers().size();
                int refund = (contract.getReward() / 2) / members;
                for (StarPlayer factionMember : faction.getActiveMembers()) {
                    factionMember.sendMail(contract.getContractor().getName(), "Trade Cargo Intercepted", "We regret to inform you that one of your faction's recent trade requests had it's cargo intercepted. We apologize for this interruption and have issued a partial refund to your member's accounts.");
                    factionMember.setCredits(factionMember.getCredits() + refund);
                }
            }
            DataUtils.removeContract(contract, true, player);
        } else {

            StarSector starSector = StarUniverse.getUniverse().getSector(new Vector3i(contract.getTarget().getLocation()[0], contract.getTarget().getLocation()[1], contract.getTarget().getLocation()[2]));
            StarSector safeSector = getNearbyRandomSector(tradeFleet.getFlagshipSector(), 5);
            tradeFleet.moveTo(safeSector);
            tradeFleet.delete();
            if (starSector.getInternalSector().getFactionId() != 0) {
                StarFaction faction = StarFaction.fromId(starSector.getInternalSector().getFactionId());
                int members = faction.getActiveMembers().size();
                int refund = (contract.getReward() / 2) / members;
                for (StarPlayer factionMember : faction.getActiveMembers()) {
                    factionMember.sendMail(contract.getContractor().getName(), "Trade Cargo Intercepted", "We regret to inform you that one of your faction's recent trade requests had it's cargo intercepted. We apologize for this interruption and have issued a partial refund to your member's accounts.");
                    factionMember.setCredits(factionMember.getCredits() + refund);
                }
            }
            DataUtils.removeContract(contract, true);
        }
        contract.setFinished(true);
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
                    cancel();
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
                    cancel();
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
                    boolean intercepted = false;
                    for (Ship ship : tradeFleet.getMembers()) {
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

                    if (intercepted) {
                        new StarRunnable() {
                            @Override
                            public void run() {
                                boolean safe = true;
                                for (Ship ship : tradeFleet.getMembers()) {
                                    for (StarEntity entity : ship.getSector().getEntities()) {
                                        if((entity.getAttachedPlayers().size() > 0 && (ship.getFaction().getPersonalEnemies().contains(entity.getPilot()) || player.getFaction().getPersonalEnemies().contains(entity.getPilot()))) || entity.getFaction().getID() == -1 || entity.getFaction().getEnemies().contains(ship.getFaction()) || entity.getFaction().getEnemies().contains(player.getFaction())) {
                                            safe = false;
                                            break;
                                        }
                                    }
                                    if(!safe) break;
                                }
                                if(safe) {
                                    PlayerUtils.sendMessage(player.getPlayerState(), "[TRADERS]: Alright, we're clear. Continuing to " + contract.getTarget().getLocation()[0] + ", " + contract.getTarget().getLocation()[1] + ", " + contract.getTarget().getLocation()[2] + ".");
                                    startCargoContract(contract, player);
                                    cancel();
                                }
                            }
                        }.runTimer(3000);
                    }
                } else {
                    new StarRunnable() {
                        @Override
                        public void run() {
                            if (player.getCurrentEntity() != null && !player.getCurrentEntity().getSector().getCoordinates().equals(tradeFleet.getInternalFleet().getFlagShip().getSector())) {
                                contract.setFinished(true);
                                DataUtils.timeoutContract(contract, player);
                                cancel();
                            }
                        }
                    }.runTimer(3000);
                }
            } else {
                boolean intercepted = false;
                for (Ship ship : tradeFleet.getMembers()) {
                    for (StarEntity entity : ship.getSector().getEntities()) {
                        if ((entity.getAttachedPlayers().size() > 0 && (ship.getFaction().getPersonalEnemies().contains(entity.getPilot()))) || entity.getFaction().getID() == -1 || entity.getFaction().getEnemies().contains(ship.getFaction())) {
                            tradeFleet.defend(tradeFleet.getFlagshipSector());
                            intercepted = true;
                            break;
                        }
                    }
                    if (intercepted) break;
                }

                if (intercepted) {
                    new StarRunnable() {
                        @Override
                        public void run() {
                            boolean safe = true;
                            for (Ship ship : tradeFleet.getMembers()) {
                                for (StarEntity entity : ship.getSector().getEntities()) {
                                    if((entity.getAttachedPlayers().size() > 0 && (ship.getFaction().getPersonalEnemies().contains(entity.getPilot()))) || entity.getFaction().getID() == -1 || entity.getFaction().getEnemies().contains(ship.getFaction())) {
                                        safe = false;
                                        break;
                                    }
                                }
                                if(!safe) break;
                            }
                            if(safe) {
                                startCargoContract(contract, null);
                                cancel();
                            }
                        }
                    }.runTimer(3000);
                }
            }
        }
    }
}
