package thederpgamer.contracts.data;

import api.DebugFile;
import api.common.GameCommon;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.PlayerNotFountException;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.contract.target.ContractTarget;
import thederpgamer.contracts.data.contract.target.ProductionTarget;
import thederpgamer.contracts.data.inventory.ItemStack;
import thederpgamer.contracts.data.player.PlayerData;
import thederpgamer.contracts.gui.contract.ContractsScrollableList;
import thederpgamer.contracts.gui.contract.PlayerContractsScrollableList;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * ServerDatabase.java
 * <Description>
 * ==================================================
 * Created 03/10/2021
 * @author TheDerpGamer
 */
public class ServerDatabase {

    public static void updatePlayerData(PlayerData playerData) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), PlayerData.class);
        ArrayList<PlayerData> toRemove = new ArrayList<>();
        for(Object playerDataObject : objectList) {
            PlayerData pData = (PlayerData) playerDataObject;
            if(pData.name.equals(playerData.name)) toRemove.add(pData);
        }

        for(PlayerData pData : toRemove) PersistentObjectUtil.removeObject(Contracts.getInstance().getSkeleton(), pData);
        PersistentObjectUtil.addObject(Contracts.getInstance().getSkeleton(), playerData);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        updateContractGUI();
    }

    public static PlayerData getPlayerData(String playerName) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), PlayerData.class);
        for(Object playerDataObject : objectList) {
            PlayerData playerData = (PlayerData) playerDataObject;
            if(playerData.name.equals(playerName)) return playerData;
        }
        return null;
    }

    public static PlayerData getPlayerData(PlayerState playerState) {
        PlayerData playerData;
        if((playerData = getPlayerData(playerState.getName())) != null) {
            return playerData;
        } else {
            return createNewPlayerData(playerState);
        }
    }

    private static PlayerData createNewPlayerData(PlayerState playerState) {
        PlayerData playerData = new PlayerData(playerState);
        PersistentObjectUtil.addObject(Contracts.getInstance().getSkeleton(), playerData);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        return playerData;
    }

    public static ArrayList<Integer> getFactionAllies(int factionId) {
        ArrayList<Integer> factionAllies = new ArrayList<>();
        Faction faction;
        if((faction = GameCommon.getGameState().getFactionManager().getFaction(factionId)) != null) {
            for(Faction ally : faction.getFriends()) factionAllies.add(ally.getIdFaction());
        }
        return factionAllies;
    }

    public static void updateContract(Contract contract) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), Contract.class);
        ArrayList<Contract> toRemove = new ArrayList<>();
        for(Object contractObject : objectList) {
            Contract c = (Contract) contractObject;
            if(c.getUID().equals(contract.getUID())) toRemove.add(c);
        }

        for(Contract c : toRemove) PersistentObjectUtil.removeObject(Contracts.getInstance().getSkeleton(), c);
        PersistentObjectUtil.addObject(Contracts.getInstance().getSkeleton(), contract);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        updateContractGUI();
    }

    public static void addContract(Contract contract) {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), Contract.class);
        ArrayList<Contract> toRemove = new ArrayList<>();
        for(Object contractObject : contractObjectList) {
            Contract c = (Contract) contractObject;
            if(c.getUID().equals(contract.getUID())) toRemove.add(c);
        }
        for(Contract c : toRemove) PersistentObjectUtil.removeObject(Contracts.getInstance().getSkeleton(), c);
        PersistentObjectUtil.addObject(Contracts.getInstance().getSkeleton(), contract);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        updateContractGUI();
    }

    public static void removeContract(Contract contract) {
        PersistentObjectUtil.removeObject(Contracts.getInstance().getSkeleton(), contract);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        updateContractGUI();
    }

    public static ArrayList<Contract> getAllContracts() {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), Contract.class);
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Object contractObject : contractObjectList) contracts.add((Contract) contractObject);
        return contracts;
    }

    public static ArrayList<Contract> getPlayerContracts(PlayerData playerData) {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(Contracts.getInstance().getSkeleton(), Contract.class);
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Object contractObject : contractObjectList) {
            Contract contract = (Contract) contractObject;
            if(contract.getClaimants().contains(playerData)) contracts.add(contract);
        }
        return contracts;
    }

    public static void updateContractGUI() {
        if(ContractsScrollableList.getInst() != null) {
            ContractsScrollableList.getInst().clear();
            ContractsScrollableList.getInst().handleDirty();
        }
        if(PlayerContractsScrollableList.getInst() != null) {
            PlayerContractsScrollableList.getInst().clear();
            PlayerContractsScrollableList.getInst().handleDirty();
        }
    }

    public static void startContractTimer(final Contract contract, final PlayerData player) {
        new StarRunnable() {
            @Override
            public void run() {
                if(contract.getTimer() >= Contracts.getInstance().contractTimerMax) {
                    if(contract.getClaimants().contains(player)) {
                        try {
                            timeoutContract(contract, player);
                        } catch(PlayerNotFountException e) {
                            e.printStackTrace();
                        }
                    } else {
                        contract.setTimer(0);
                    }
                    cancel();
                } else {
                    contract.setTimer(contract.getTimer() + 1);
                    if(Contracts.getInstance().debugMode) {
                        DebugFile.log("[DEBUG]: Contract timer: " + contract.getTimer(), Contracts.getInstance());
                        DebugFile.log("[DEBUG]: for contract " + contract.getUID(), Contracts.getInstance());
                    }
                }
            }
        }.runTimer(Contracts.getInstance(), 1000);
    }

    public static void timeoutContract(Contract contract, PlayerData player) throws PlayerNotFountException {
        contract.getClaimants().remove(player);
        assert player != null;
        player.contracts.remove(contract);
        addContract(contract);
        updatePlayerData(player);
        player.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled your contract because you took too long!");
    }

    public static void generateRandomContract() {
        Random random = new Random();
        int contractTypeInt = random.nextInt(2) + 1;
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
                ItemStack productionStack = new ItemStack(ElementKeyMap.getInfo(productionID));
                productionStack.count = amountInt;
                basePrice = (int) ElementKeyMap.getInfo(productionID).getPrice(true);
                ItemStack[] productionStacks = new ItemStack[]{productionStack};
                target.setTargets(productionStacks);
                break;
            case 2:
                contractType = Contract.ContractType.MINING;
                for(ElementInformation info : getResourcesFilter()) possibleIDs.add(info.getId());
                int miningIndex = random.nextInt(possibleIDs.size() - 1) + 1;
                short miningID = possibleIDs.get(miningIndex);
                contractName = "Produce x" + amountInt + " " + ElementKeyMap.getInfo(miningID).getName();
                target = new ProductionTarget();
                ItemStack miningStack = new ItemStack(ElementKeyMap.getInfo(miningID));
                miningStack.count = amountInt;
                basePrice = (int) ElementKeyMap.getInfo(miningID).getPrice(true);
                ItemStack[] miningStacks = new ItemStack[]{miningStack};
                target.setTargets(miningStacks);
                break;
        }
        int reward = (int) ((basePrice * amountInt) * 1.3);

        Contract randomContract = new Contract(Contracts.getInstance().tradersFactionID, contractName, contractType, reward, UUID.randomUUID().toString(), target);
        addContract(randomContract);
    }

    public static ArrayList<ElementInformation> getResourcesFilter() {
        ArrayList<ElementInformation> filter = new ArrayList<>();
        ArrayList<ElementInformation> elementList = new ArrayList<>();
        ElementKeyMap.getCategoryHirarchy().getChild("Manufacturing").getInfoElementsRecursive(elementList);
        for(ElementInformation info : elementList) {
            if(!info.isDeprecated() && info.isShoppable() && info.isInRecipe()) filter.add(info);
        }
        return filter;
    }

    public static ArrayList<ElementInformation> getProductionFilter() {
        ArrayList<ElementInformation> filter = new ArrayList<>();
        ArrayList<ElementInformation> elementList = new ArrayList<>();
        ElementKeyMap.getCategoryHirarchy().getChild("General").getInfoElementsRecursive(elementList);
        ElementKeyMap.getCategoryHirarchy().getChild("Ship").getInfoElementsRecursive(elementList);
        ElementKeyMap.getCategoryHirarchy().getChild("SpaceStation").getInfoElementsRecursive(elementList);
        for(ElementInformation info : elementList) {
            if(!info.isDeprecated() && info.isShoppable() && info.isInRecipe()) filter.add(info);
        }
        return filter;
    }
}
