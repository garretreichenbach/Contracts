package dovtech.contracts.util;

import api.DebugFile;
import api.common.GameServer;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.network.packets.PacketUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.faction.FactionOpinion;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.network.client.GetAllContractsPacket;
import dovtech.contracts.network.client.GetClientContractsPacket;
import dovtech.contracts.network.client.GetPlayerDataPacket;
import dovtech.contracts.player.PlayerData;
import org.schema.game.common.data.player.faction.Faction;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class DataUtils {

    private static final Contracts instance = Contracts.getInstance();
    private static final boolean debug = Contracts.getInstance().debugMode;
    private static final File contractsFolder = new File("moddata/Contracts/contractdata");
    private static final File playerDataFolder = new File("moddata/Contracts/playerdata");

    private static HashMap<String, Contract> contracts = new HashMap<>();
    private static HashMap<String, PlayerData> players = new HashMap<>();
    private static ArrayList<Contract> contractWriteBuffer = new ArrayList<>();
    private static ArrayList<PlayerData> playerDataWriteBuffer = new ArrayList<>();

    public static PlayerData getPlayerData(String name) {
        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            return players.get(name);
        } else {
            return getUpdatedPlayerData();
        }
    }

    public static void addContract(Contract contract) {
        contracts.put(contract.getUID(), contract);
        contractWriteBuffer.add(contract);
    }

    public static void addPlayer(PlayerData player) {
        players.put(player.getName(), player);
        playerDataWriteBuffer.add(player);
    }

    private static PlayerData getUpdatedPlayerData() {
        GetPlayerDataPacket getPlayerDataPacket = new GetPlayerDataPacket();
        PacketUtil.sendPacketToServer(getPlayerDataPacket);
        return getPlayerDataPacket.getPlayerData();
    }

    public static ArrayList<Contract> getPlayerContracts(String name) {
        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            ArrayList<Contract> playerContracts = new ArrayList<>();
            for(Contract contract :  contracts.values()) {
                if(getPlayerData(name).getContractUIDs().contains(contract.getUID())) {
                    playerContracts.add(contract);
                }
            }
            return playerContracts;
        } else {
            GetClientContractsPacket getClientContractsPacket = new GetClientContractsPacket();
            PacketUtil.sendPacketToServer(getClientContractsPacket);
            return getClientContractsPacket.getPlayerContracts();
        }
    }

    public static ArrayList<Contract> getAllContracts() {
        if (Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            return (ArrayList<Contract>) contracts.values();
        } else {
            GetAllContractsPacket getAllContractsPacket = new GetAllContractsPacket();
            PacketUtil.sendPacketToServer(getAllContractsPacket);
            return getAllContractsPacket.getContracts();
        }
    }

    public static void readData() throws IOException, ClassNotFoundException {
        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            if(contractsFolder.listFiles() != null) {
                for(File contractFile : contractsFolder.listFiles()) {
                    FileInputStream inputStream = new FileInputStream(contractFile);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    Contract contract = (Contract) objectInputStream.readObject();
                    objectInputStream.close();
                    inputStream.close();
                    contracts.put(contract.getUID(), contract);
                }
            } else {
                if(debug) DebugFile.log("[DEBUG]: Contracts folder is empty, not reading...", instance);
            }

            if(playerDataFolder.listFiles() != null) {
                for(File playerDataFile : playerDataFolder.listFiles()) {
                    FileInputStream inputStream = new FileInputStream(playerDataFile);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    PlayerData player = (PlayerData) objectInputStream.readObject();
                    objectInputStream.close();
                    inputStream.close();
                    players.put(player.getName(), player);
                }
            } else {
                if(debug) DebugFile.log("[DEBUG]: PlayerData folder is empty, not reading...", instance);
            }
        }
    }

    public static void writeData() {
        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {

            for (Contract contract : contractWriteBuffer) {
                try {
                    File contractFile = new File(contractsFolder.getAbsolutePath() + "/" + contract.getUID() + ".smdat");
                    if (contractFile.exists()) contractFile.delete();
                    contractFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(contractFile);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(contract);
                    objectOutputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    DebugFile.log("[ERROR]: Something went wrong while trying to write contract " + contract.getName() + " to file!");
                }
            }
            contractWriteBuffer.clear();

            for (PlayerData playerData : playerDataWriteBuffer) {
                try {
                    File playerDataFile = new File(playerDataFolder.getAbsolutePath() + "/" + playerData.getName() + ".smdat");
                    if (playerDataFile.exists()) playerDataFile.delete();
                    playerDataFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(playerDataFile);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(playerData);
                    objectOutputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    DebugFile.log("[ERROR]: Something went wrong while trying to write player " + playerData.getName() + " to file!");
                }
            }
            playerDataWriteBuffer.clear();
        }
    }

    public static void timeoutContract(Contract contract, StarPlayer player) {
        PlayerData pData = players.get(player.getName());
        contract.removeClaimant(player);
        pData.removeContract(contract);
        contracts.put(contract.getUID(), contract);
        players.put(player.getName(), pData);
        contractWriteBuffer.add(contract);
        playerDataWriteBuffer.add(pData);
        player.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled your contract because you took too long!");
    }

    public static void removeContract(Contract contract, boolean canceled, StarPlayer... claimer) {
        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {

            if (claimer != null) {
                ArrayList<StarPlayer> claimants = contract.getClaimants();
                for (StarPlayer p : claimants) {
                    PlayerData pData = players.get(p.getName());
                    contract.removeClaimant(p);
                    pData.removeContract(contract);
                    if (canceled) {
                        p.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled contract " + contract.getName() + ".");
                    } else if (claimer[0].getName().equals(p.getName())) {
                        p.sendMail(contract.getContractor().getName(), "Contract Completed", "We hear you have completed the contract and have sent the reward money to your account. It's been a pleasure doing business with you.");
                    } else {
                        p.sendMail(contract.getContractor().getName(), "Contract Ended", claimer[0].getName() + " has claimed the reward for contract " + contract.getName() + ".");
                    }
                    players.put(pData.getName(), pData);
                    playerDataWriteBuffer.add(pData);
                }
            }

            contracts.remove(contract.getUID());

            for (File contractFile : Objects.requireNonNull(contractsFolder.listFiles())) {
                if (contractFile.getName().substring(0, contractFile.getName().indexOf(".") - 1).equals(contract.getUID())) {
                    contractFile.delete();
                    break;
                }
            }
        }

        if(Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER) || Contracts.getInstance().gameState.equals(Contracts.Mode.CLIENT)) {
            if(ContractsScrollableList.getInst() != null) {
                ContractsScrollableList.getInst().clear();
                ContractsScrollableList.getInst().handleDirty();
            }
            if(PlayerContractsScrollableList.getInst() != null) {
                PlayerContractsScrollableList.getInst().clear();
                PlayerContractsScrollableList.getInst().handleDirty();
            }
        }
    }

    public static FactionOpinion[] genOpinions() {
        if (Contracts.getInstance().gameState.equals(Contracts.Mode.SERVER) || Contracts.getInstance().gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            Collection<Faction> factions = GameServer.getServerState().getFactionManager().getFactionCollection();
            FactionOpinion[] opinions = new FactionOpinion[factions.size()];
            int i = 0;
            for (Faction f : factions) {
                StarFaction faction = new StarFaction(f);
                if (faction.getInternalFaction().isNPC() && faction.getID() == Contracts.getInstance().tradersFactionID) {
                    opinions[i] = new FactionOpinion(faction.getID(), 15);
                } else if (faction.getName().toLowerCase().contains("pirate") || faction.getID() == -1) {
                    opinions[i] = new FactionOpinion(faction.getID(), -40);
                } else {
                    opinions[i] = new FactionOpinion(faction.getID(), 0);
                }
                i++;
            }
            return opinions;
        } else {
            return null;
        }
    }
}
