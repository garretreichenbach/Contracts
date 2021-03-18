package thederpgamer.contracts.data;

import api.DebugFile;
import api.common.GameCommon;
import api.mod.ModSkeleton;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.PlayerNotFountException;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.inventory.ItemStack;
import thederpgamer.contracts.data.player.PlayerData;
import thederpgamer.contracts.gui.contract.contractlist.ContractsScrollableList;
import thederpgamer.contracts.gui.contract.playercontractlist.PlayerContractsScrollableList;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * ServerDatabase.java
 * Manages server data and contains functions for saving and loading it from file.
 *
 * @since 03/10/2021
 * @author TheDerpGamer
 */
public class ServerDatabase {

   private static final ModSkeleton instance = Contracts.getInstance().getSkeleton();

    /**
     * Locates any existing playerData matching the updated playerData's name and replaces them with the updated version.
     * @param playerData The updated PlayerData.
     */
    public static void updatePlayerData(PlayerData playerData) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(instance, PlayerData.class);
        ArrayList<PlayerData> toRemove = new ArrayList<>();
        for(Object playerDataObject : objectList) {
            PlayerData pData = (PlayerData) playerDataObject;
            if(pData.name.equals(playerData.name)) toRemove.add(pData);
        }

        for(PlayerData pData : toRemove) PersistentObjectUtil.removeObject(instance, pData);
        PersistentObjectUtil.addObject(instance, playerData);
    }

    /**
     * Gets the PlayerData for a player based off their name.
     * @param playerName The player's name.
     * @return The player's PlayerData. Returns null if no matching data is found with the specified name.
     */
    public static PlayerData getPlayerData(String playerName) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(instance, PlayerData.class);
        for(Object playerDataObject : objectList) {
            PlayerData playerData = (PlayerData) playerDataObject;
            if(playerData.name.equals(playerName)) return playerData;
        }
        return null;
    }

    /**
     * Gets the PlayerData for a player based off their PlayerState.
     * @param playerState The player's state.
     * @return The player's PlayerData. Creates a new entry if no existing PlayerData is found matching the PlayerState.
     */
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
        PersistentObjectUtil.addObject(instance, playerData);
        return playerData;
    }

    /**
     * Creates an ArrayList containing the ids of a specified faction's allies and returns it.
     * @param factionId The faction's id.
     * @return An ArrayList containing the ids of the faction's allies.
     */
    public static ArrayList<Integer> getFactionAllies(int factionId) {
        ArrayList<Integer> factionAllies = new ArrayList<>();
        Faction faction;
        if((faction = GameCommon.getGameState().getFactionManager().getFaction(factionId)) != null) {
            for(Faction ally : faction.getFriends()) factionAllies.add(ally.getIdFaction());
        }
        return factionAllies;
    }

    /**
     * Locates any existing contracts matching the updated contract's id and replaces them with the updated version.
     * @param contract The updated Contract.
     */
    public static void updateContract(Contract contract) {
        ArrayList<Object> objectList = PersistentObjectUtil.getObjects(instance, Contract.class);
        ArrayList<Contract> toRemove = new ArrayList<>();
        for(Object contractObject : objectList) {
            Contract c = (Contract) contractObject;
            if(c.getUID().equals(contract.getUID())) toRemove.add(c);
        }

        for(Contract c : toRemove) PersistentObjectUtil.removeObject(instance, c);
        PersistentObjectUtil.addObject(instance, contract);
    }

    /**
     * Adds a new Contract to the database.
     * @param contract The new contract.
     */
    public static void addContract(Contract contract) {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(instance, Contract.class);
        ArrayList<Contract> toRemove = new ArrayList<>();
        for(Object contractObject : contractObjectList) {
            Contract c = (Contract) contractObject;
            if(c.getUID().equals(contract.getUID())) toRemove.add(c);
        }
        for(Contract c : toRemove) PersistentObjectUtil.removeObject(instance, c);
        PersistentObjectUtil.addObject(instance, contract);
    }

    /**
     * Removes any contracts from the database that have the same id as the specified contract.
     * @param contract The contract to remove.
     */
    public static void removeContract(Contract contract) {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(instance, Contract.class);
        ArrayList<Contract> toRemove = new ArrayList<>();
        for(Object contractObject : contractObjectList) {
            Contract c = (Contract) contractObject;
            if(c.getUID().equals(contract.getUID())) toRemove.add(c);
        }
        for(Contract c : toRemove) PersistentObjectUtil.removeObject(instance, c);
    }

    /**
     * Creates an ArrayList containing all Contracts in the database and returns it.
     * @return An ArrayList containing all Contracts.
     */
    public static ArrayList<Contract> getAllContracts() {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(instance, Contract.class);
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Object contractObject : contractObjectList) contracts.add((Contract) contractObject);
        return contracts;
    }

    /**
     * Creates an ArrayList containing all Contracts a specified player has claimed and returns it.
     * @param playerData The player's data.
     * @return An ArrayList containing the player's claimed contracts.
     */
    public static ArrayList<Contract> getPlayerContracts(PlayerData playerData) {
        ArrayList<Object> contractObjectList = PersistentObjectUtil.getObjects(instance, Contract.class);
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Object contractObject : contractObjectList) {
            Contract contract = (Contract) contractObject;
            if(contract.getClaimants().contains(playerData)) contracts.add(contract);
        }
        return contracts;
    }

    /**
     * Updates the Contract List GUIs (ContractsScrollableList and PlayerContractsScrollableList) and redraws them.
     */
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

    /**
     * Starts a timer in which the specified player must complete the contract before it reaches 0.
     * @param contract The contract being started.
     * @param player The player's data.
     */
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

    /**
     * Removes a player's claim from a contract if they have not completed it before the contract's timer reaches 0.
     * @param contract The contract being removed.
     * @param player The player who claimed the contract.
     * @throws PlayerNotFountException
     */
    public static void timeoutContract(Contract contract, PlayerData player) throws PlayerNotFountException {
        contract.getClaimants().remove(player);
        assert player != null;
        player.contracts.remove(contract);
        updateContract(contract);
        updatePlayerData(player);
        player.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled your contract because you took too long!");
    }

    /**
     * Generates a random contract and adds it to the list.
     */
    public static void generateRandomContract() {
        Random random = new Random();
        int contractTypeInt = random.nextInt(2) + 1;
        Contract.ContractType contractType = null;
        ArrayList<Short> possibleIDs = new ArrayList<>();
        String contractName = "";

        int amountInt = random.nextInt(3000 - 100) + 100;
        int basePrice = 0;
        ItemStack target = null;
        switch(contractTypeInt) {
            case 1:
                contractType = Contract.ContractType.PRODUCTION;
                for(ElementInformation info : getProductionFilter()) possibleIDs.add(info.getId());
                int productionIndex = random.nextInt(possibleIDs.size() - 1) + 1;
                short productionID = possibleIDs.get(productionIndex);
                contractName = "Produce x" + amountInt + " " + ElementKeyMap.getInfo(productionID).getName();
                target = new ItemStack(ElementKeyMap.getInfo(productionID));
                target.count = amountInt;
                basePrice = (int) ElementKeyMap.getInfo(productionID).getPrice(true);
                break;
            case 2:
                contractType = Contract.ContractType.MINING;
                for(ElementInformation info : getResourcesFilter()) possibleIDs.add(info.getId());
                int miningIndex = random.nextInt(possibleIDs.size() - 1) + 1;
                short miningId = possibleIDs.get(miningIndex);
                contractName = "Mine x" + amountInt + " " + ElementKeyMap.getInfo(miningId).getName();
                target = new ItemStack(ElementKeyMap.getInfo(miningId));
                target.count = amountInt;
                basePrice = (int) ElementKeyMap.getInfo(miningId).getPrice(true);
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
            if(!info.isDeprecated() && info.isShoppable() && info.isInRecipe() && !info.getName().contains("Paint")) filter.add(info);
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
