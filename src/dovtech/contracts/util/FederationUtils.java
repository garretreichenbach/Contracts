package dovtech.contracts.util;

import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.federation.Federation;
import dovtech.contracts.network.client.GetFederationsPacket;
import java.util.ArrayList;
import java.util.Objects;

public class FederationUtils {

    private static final Contracts instance = Contracts.getInstance();
    public static ArrayList<Federation> clientFederationList = new ArrayList<>();

    public static Federation getFederationFromID(int federationID) {
        if (instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for (Object object : PersistentObjectUtil.getObjects(instance, Federation.class)) { //Search database
                Federation federation = (Federation) object;
                if (federation.getFederationID() == federationID) return federation;
            }
        } else if (instance.getGameState().equals(Contracts.Mode.CLIENT)) {
            for (Federation federation : Objects.requireNonNull(getAllFederations())) {
                if (federation.getFederationID() == federationID) return federation;
            }
        }
        return null; //Federation not found
    }

    public static ArrayList<Federation> getAllFederations() {
        if (instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            ArrayList<Federation> federations = new ArrayList<>();
            for (Object object : PersistentObjectUtil.getObjects(instance, Federation.class)) {
                Federation federation = (Federation) object;
                federations.add(federation);
            }
            return federations;
        } else if (instance.getGameState().equals(Contracts.Mode.CLIENT)) {
            GetFederationsPacket getFederationsPacket = new GetFederationsPacket();
            PacketUtil.sendPacketToServer(getFederationsPacket);
            return clientFederationList;
        }
        return null;
    }
}
