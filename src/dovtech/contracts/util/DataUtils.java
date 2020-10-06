package dovtech.contracts.util;

import api.common.GameClient;
import api.common.GameCommon;
import api.common.GameServer;
import api.entity.StarPlayer;
import api.entity.StarStation;
import api.faction.StarFaction;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import api.universe.StarSector;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.faction.FactionOpinion;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.network.client.*;
import dovtech.contracts.player.PlayerData;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.PlayerNotFountException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class DataUtils {

    private static final Contracts instance = Contracts.getInstance();
    public static PlayerData playerData;
    private static ArrayList<PlayerData> localPlayers = new ArrayList<>();
    public static ArrayList<Contract> localPlayerContracts = new ArrayList<>();
    public static ArrayList<Contract> localContracts = new ArrayList<>();
    public static ArrayList<Integer> localFactionAllies = new ArrayList<>();
    public static int clientSectorStationFaction = 0;

    public static int getSectorStationFactionID(StarPlayer player) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            StarSector sector = player.getSector();
            if(sector.getStations() == null || sector.getStations().size() == 0 || sector.getStations().get(0).getFaction() == null) {
                return 0;
            } else {
                return sector.getStations().get(0).getFaction().getID();
            }
        } else {
            GetClientSectorStationFactionPacket getClientSectorStationFactionPacket = new GetClientSectorStationFactionPacket();
            PacketUtil.sendPacketToServer(getClientSectorStationFactionPacket);
            return clientSectorStationFaction;
        }
    }

    public static ArrayList<Integer> getAllies(int playerFactionID) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            ArrayList<Integer> allies = new ArrayList<>();
            FactionManager factionManager = GameServer.getServerState().getFactionManager();
            for(Faction faction : factionManager.getFaction(playerFactionID).getFriends()) allies.add(faction.getIdFaction());
            return allies;
        } else {
            GetFactionAlliesPacket getFactionAlliesPacket = new GetFactionAlliesPacket(playerFactionID);
            PacketUtil.sendPacketToServer(getFactionAlliesPacket);
        }

        return localFactionAllies;
    }

    public static PlayerData getPlayerData(String name) throws PlayerNotFountException {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for(Object object : PersistentObjectUtil.getObjects(instance, PlayerData.class)) {
                PlayerData pData = (PlayerData) object;
                if(pData.getName().equals(name)) return pData;
            }
            PlayerData pData = new PlayerData(new StarPlayer(GameServer.getServerState().getPlayerFromName(name)));
            addPlayer(pData);
            return pData;
        } else {
            return getUpdatedPlayerData(name);
        }
    }

    public static void addPlayerDataToLocal(PlayerData pData) {
        for(PlayerData p : localPlayers) {
            if(p.getName().equals(pData.getName())) {
                localPlayers.remove(p);
                localPlayers.add(pData);
                return;
            }
        }
        localPlayers.add(pData);
    }

    public static void addContract(Contract contract) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for(Object object : PersistentObjectUtil.getObjects(instance, Contract.class)) {
                Contract c = (Contract) object;
                if(c.getUID().equals(contract.getUID())) {
                    PersistentObjectUtil.removeObject(instance, c);
                    PersistentObjectUtil.addObject(instance, contract);
                    PersistentObjectUtil.save(instance);
                    if(ContractsScrollableList.getInst() != null) {
                        ContractsScrollableList.getInst().clear();
                        ContractsScrollableList.getInst().handleDirty();
                    }
                    return;
                }
            }
            PersistentObjectUtil.addObject(instance, contract);
            PersistentObjectUtil.save(instance);
        } else {
            AddContractPacket addContractPacket = new AddContractPacket(contract);
            PacketUtil.sendPacket(GameClient.getClientPlayerState(), addContractPacket);
        }
        if(ContractsScrollableList.getInst() != null) {
            try {
                ContractsScrollableList.getInst().clear();
                ContractsScrollableList.getInst().handleDirty();
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void addPlayer(PlayerData player) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for (Object object : PersistentObjectUtil.getObjects(instance, PlayerData.class)) {
                PlayerData pData = (PlayerData) object;
                if (pData.getName().equals(player.getName())) {
                    PersistentObjectUtil.removeObject(instance, pData);
                    PersistentObjectUtil.addObject(instance, player);
                    PersistentObjectUtil.save(instance);
                    return;
                }
            }
            PersistentObjectUtil.addObject(instance, player);
            PersistentObjectUtil.save(instance);
        }
    }

    private static PlayerData getUpdatedPlayerData(String playerName) {
        GetPlayerDataPacket getPlayerDataPacket = new GetPlayerDataPacket(playerName);
        PacketUtil.sendPacketToServer(getPlayerDataPacket);
        for(PlayerData playerData : localPlayers) {
            if(playerData.getName().equals(playerName)) return playerData;
        }
        return null;
    }

    public static ArrayList<Contract> getPlayerContracts(String name) throws PlayerNotFountException {
        if(Contracts.getInstance().getGameState().equals(Contracts.Mode.SERVER) || Contracts.getInstance().getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            ArrayList<Contract> playerContracts = new ArrayList<>();
            for(Object object : PersistentObjectUtil.getObjects(instance, Contract.class)) {
                Contract contract = (Contract) object;
                if(getPlayerData(name).getContractUIDs().contains(contract.getUID())) {
                    playerContracts.add(contract);
                }
            }
            return playerContracts;
        } else {
            GetClientContractsPacket getClientContractsPacket = new GetClientContractsPacket();
            PacketUtil.sendPacketToServer(getClientContractsPacket);
            return localPlayerContracts;
        }
    }

    public static ArrayList<Contract> getAllContracts() {
        if (Contracts.getInstance().getGameState().equals(Contracts.Mode.SERVER) || Contracts.getInstance().getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            ArrayList<Contract> contracts = new ArrayList<>();
            for(Object object : PersistentObjectUtil.getObjects(instance, Contract.class)) {
                contracts.add((Contract) object);
            }
            return contracts;
        } else {
            GetAllContractsPacket getAllContractsPacket = new GetAllContractsPacket();
            PacketUtil.sendPacketToServer(getAllContractsPacket);
            return localContracts;
        }
    }

    public static void timeoutContract(Contract contract, StarPlayer player) throws PlayerNotFountException {
        PlayerData pData = getPlayerData(player.getName());
        contract.removeClaimant(player);
        assert pData != null;
        pData.removeContract(contract);
        addContract(contract);
        addPlayer(pData);

        player.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled your contract because you took too long!");
    }

    public static void cancelContract(String contractUID) throws PlayerNotFountException {
        for(Contract contract : getAllContracts()) {
            if(contract.getUID().equals(contractUID)) {
                removeContract(contract, true);
                return;
            }
        }
    }

    public static void removeContract(Contract contract, boolean canceled, StarPlayer... claimer) throws PlayerNotFountException {
        if(Contracts.getInstance().getGameState().equals(Contracts.Mode.SERVER) || Contracts.getInstance().getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            if (claimer != null) {
                ArrayList<StarPlayer> claimants = contract.getClaimants();
                for (StarPlayer p : claimants) {
                    PlayerData pData = getPlayerData(p.getName());
                    contract.removeClaimant(p);
                    assert pData != null;
                    pData.removeContract(contract);
                    if (canceled) {
                        p.sendMail(contract.getContractor().getName(), "Contract Cancellation", contract.getContractor().getName() + " has cancelled contract " + contract.getName() + ".");
                    } else if (claimer[0].getName().equals(p.getName())) {
                        p.sendMail(contract.getContractor().getName(), "Contract Completed", "We hear you have completed the contract and have sent the reward money to \nyour account. It's been a pleasure doing business with you.");
                    } else {
                        p.sendMail(contract.getContractor().getName(), "Contract Ended", claimer[0].getName() + " has claimed the reward for contract " + contract.getName() + ".");
                    }
                    addPlayer(pData);
                    addContract(contract);
                }
            }

            PersistentObjectUtil.removeObject(instance, contract);
            PersistentObjectUtil.save(instance);
        }

        if(Contracts.getInstance().getGameState().equals(Contracts.Mode.SINGLEPLAYER) || Contracts.getInstance().getGameState().equals(Contracts.Mode.CLIENT)) {
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

    public static void genOpinions(PlayerData pData) {
        Collection<Faction> factions = StarLoader.getGameState().getFactionManager().getFactionCollection();
        ArrayList<Integer> factionIds = new ArrayList<>();
        ArrayList<FactionOpinion> newOpinionsList;
        if(pData.getOpinions() == null) {
            newOpinionsList = new ArrayList<>();
        } else {
           newOpinionsList = new ArrayList<>(Arrays.asList(pData.getOpinions()));
            for (FactionOpinion opinion : Objects.requireNonNull(pData.getOpinions())) {
                factionIds.add(opinion.getFaction().getID());
            }
        }

        for (Faction faction : factions) {
            int factionID = faction.getIdFaction();
            if (!factionIds.contains(factionID) && pData.getFactionID() != factionID) {
                if (faction.isNPC() && faction.getIdFaction() == Contracts.getInstance().tradersFactionID) {
                    newOpinionsList.add(new FactionOpinion(faction.getIdFaction(), 15));
                } else if (faction.getName().toLowerCase().contains("pirate") || faction.getIdFaction() == -1) {
                    newOpinionsList.add(new FactionOpinion(faction.getIdFaction(), -40));
                } else {
                    newOpinionsList.add(new FactionOpinion(faction.getIdFaction(), 0));
                }
            }
        }
        FactionOpinion[] newOpinions = new FactionOpinion[newOpinionsList.size()];
        for(int i = 0; i < newOpinions.length; i ++) {
            newOpinions[i] = newOpinionsList.get(i);
        }
        pData.setOpinions(newOpinions);
        addPlayer(pData);
    }
}
