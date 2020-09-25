package dovtech.contracts.util;

import api.DebugFile;
import api.entity.StarPlayer;
import com.google.gson.Gson;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import java.io.*;
import java.util.ArrayList;

public class DataUtil {

    private static final Contracts instance = Contracts.getInstance();
    private static final boolean debug = Contracts.getInstance().debugMode;
    private static final File contractsFolder = new File("moddata/Contracts/contract");
    private static final File playerDataFolder = new File("moddata/Contracts/player");

    public static ArrayList<Contract> contracts = new ArrayList<>();
    public static ArrayList<PlayerData> players = new ArrayList<>();
    public static ArrayList<Contract> contractWriteBuffer = new ArrayList<>();
    public static ArrayList<PlayerData> playerDataWriteBuffer = new ArrayList<>();

    public static PlayerData getPlayerData(StarPlayer player) {
        for(PlayerData playerData : players) {
            if(playerData.getPlayerName().equals(player.getName())) return playerData;
        }
        PlayerData newPlayerData = new PlayerData(player);
        players.add(newPlayerData);
        playerDataWriteBuffer.add(newPlayerData);
        return newPlayerData;
    }

    public static Contract getUpdatedContract(Contract contract) {
        for(Contract c : contracts) {
            if(c.getUid().equals(contract.getUid())) return c;
        }
        return contract;
    }

    public static void readData() throws IOException {
        Gson gson = new Gson();
        if(contractsFolder.listFiles() != null) {
            for(File contractFile : contractsFolder.listFiles()) {
                BufferedReader br = new BufferedReader(new FileReader(contractFile));
                contracts.add(gson.fromJson(br, Contract.class));
                br.close();
            }
        } else {
            if(debug) DebugFile.log("[DEBUG]: Contracts folder is empty, not reading...", instance);
        }

        if(playerDataFolder.listFiles() != null) {
            for(File playerDataFile : playerDataFolder.listFiles()) {
                BufferedReader br = new BufferedReader(new FileReader(playerDataFile));
                players.add(gson.fromJson(br, PlayerData.class));
                br.close();
            }
        } else {
            if(debug) DebugFile.log("[DEBUG]: PlayerData folder is empty, not reading...", instance);
        }
    }

    public static void writeData() {
        Gson gson = new Gson();
        for(Contract contract : contractWriteBuffer) {
            try {
                File contractFile = new File(contractsFolder.getAbsolutePath() + "/" + contract.getUid() + ".smdat");
                if(contractFile.exists()) contractFile.delete();
                contractFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(contractFile));
                bw.write(gson.toJson(contract));
                bw.close();
                if (!contracts.contains(contract)) contracts.add(contract);
            } catch (IOException e) {
                e.printStackTrace();
                DebugFile.log("[ERROR]: Something went wrong while trying to write contract " + contract.getName() + " to file!");
                /*
                if(debug) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(contract.getClaimants().get(0).getName());
                    for(int i = 1; i < contract.getClaimants().size(); i ++) builder.append(", ").append(contract.getClaimants().get(i).getName());
                    DebugFile.log("[DEBUG]: Error Info[contractWriteBuffer.size() = " + contractWriteBuffer.size() + ", contractUID = '" + contract.getUid() + "', contractClaimants = [" + builder.toString() + "]]");
                }
                 */
            }
        }
        contractWriteBuffer.clear();

        for(PlayerData playerData : playerDataWriteBuffer) {
            try {
                File playerDataFile = new File(playerDataFolder.getAbsolutePath() + "/" + playerData.getPlayerName() + ".smdat");
                if(playerDataFile.exists()) playerDataFile.delete();
                playerDataFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(playerDataFile));
                bw.write(gson.toJson(playerData));
                bw.close();
                playerDataWriteBuffer.remove(playerData);
                if (!players.contains(playerData)) players.add(playerData);
            } catch (IOException e) {
                e.printStackTrace();
                DebugFile.log("[ERROR]: Something went wrong while trying to write player " + playerData.getPlayerName() + " to file!");
                /*
                if(debug) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(playerData.getContracts().get(0).getUid());
                    for(int i = 1; i < playerData.getContracts().size(); i ++) builder.append(", ").append(playerData.getContracts().get(i).getUid()));
                    DebugFile.log("[DEBUG]: Error Info[playerDataWriteBuffer.size() = " + playerDataWriteBuffer.size() + ", player = " + playerData.getPlayerName() + ", playerContracts = [" + builder.toString() + "]]");
                }
                 */
            }
        }
        playerDataWriteBuffer.clear();
    }

    public static void removeContract(Contract contract) {
        ArrayList<StarPlayer> claimants = contract.getClaimants();
        for(StarPlayer p : claimants) {
            PlayerData pData = getPlayerData(p);
            players.remove(pData);
            pData.getContracts().remove(contract);
            p.sendMail(contract.getContractor().getName(), "Contract Cancellation",contract.getContractor().getName() + " has cancelled contract " + contract.getName() + ".");
            players.add(pData);
            playerDataWriteBuffer.add(pData);
            contract.getClaimants().remove(p);
        }
        contracts.remove(contract);

        for(File contractFile : contractsFolder.listFiles()) {
            if(contractFile.getName().substring(0, contractFile.getName().indexOf(".") - 1).equals(contract.getUid())) {
                contractFile.delete();
                break;
            }
        }
    }
}
