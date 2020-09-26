package dovtech.contracts.util;

import api.DebugFile;
import api.entity.StarPlayer;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.player.PlayerData;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DataUtil {

    private static final Contracts instance = Contracts.getInstance();
    private static final boolean debug = Contracts.getInstance().debugMode;
    private static final File contractsFolder = new File("moddata/Contracts/contractdata");
    private static final File playerDataFolder = new File("moddata/Contracts/playerdata");

    public static HashMap<String, Contract> contracts = new HashMap<>();
    public static HashMap<String, PlayerData> players = new HashMap<>();
    public static ArrayList<Contract> contractWriteBuffer = new ArrayList<>();
    public static ArrayList<PlayerData> playerDataWriteBuffer = new ArrayList<>();

    public static void readData() throws IOException, ClassNotFoundException {
        if(contractsFolder.listFiles() != null) {
            for(File contractFile : contractsFolder.listFiles()) {
                FileInputStream inputStream = new FileInputStream(contractFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                Contract contract = (Contract) objectInputStream.readObject();
                objectInputStream.close();
                inputStream.close();
                contracts.put(contract.getUid(), contract);
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
                players.put(player.getPlayerName(), player);
            }
        } else {
            if(debug) DebugFile.log("[DEBUG]: PlayerData folder is empty, not reading...", instance);
        }
    }

    public static void writeData() {
        for(Contract contract : contractWriteBuffer) {
            try {
                File contractFile = new File(contractsFolder.getAbsolutePath() + "/" + contract.getUid() + ".smdat");
                if(contractFile.exists()) contractFile.delete();
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

        for(PlayerData playerData : playerDataWriteBuffer) {
            try {
                File playerDataFile = new File(playerDataFolder.getAbsolutePath() + "/" + playerData.getPlayerName() + ".smdat");
                if(playerDataFile.exists()) playerDataFile.delete();
                playerDataFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(playerDataFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(playerData);
                objectOutputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                DebugFile.log("[ERROR]: Something went wrong while trying to write player " + playerData.getPlayerName() + " to file!");
            }
        }
        playerDataWriteBuffer.clear();
    }

    public static void removeContract(Contract contract) {
        ArrayList<StarPlayer> claimants = contract.getClaimants();
        for(StarPlayer p : claimants) {
            PlayerData pData = players.get(p.getName());
            contract.removeClaimant(p);
            pData.removeContract(contract);
            p.sendMail(contract.getContractor().getName(), "Contract Cancellation",contract.getContractor().getName() + " has cancelled contract " + contract.getName() + ".");
            players.put(pData.getPlayerName(), pData);
            playerDataWriteBuffer.add(pData);
        }
        contracts.remove(contract.getUid());

        for(File contractFile : contractsFolder.listFiles()) {
            if(contractFile.getName().substring(0, contractFile.getName().indexOf(".") - 1).equals(contract.getUid())) {
                contractFile.delete();
                break;
            }
        }
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
