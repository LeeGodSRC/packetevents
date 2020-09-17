package io.github.retrooper.packetevents.packetwrappers.out.login;

import io.github.retrooper.packetevents.packet.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.WorldType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WrappedPacketOutLogin extends WrappedPacket implements SendableWrapper {

    private static Class<?> PACKET_CLASS, WORLD_TYPE_CLASS;
    private static Class<? extends Enum> ENUM_GAMEMODE_CLASS, ENUM_DIFFICULTY_CLASS;

    private static Object ENUM_GAMEMODE_NOT_SET;

    private static Method NMS_WORLD_TYPE_GET_BY_NAME, NMS_WORLD_TYPE_NAME;
    private static Constructor<?> PACKET_CONSTRUCTOR;

    private int playerId;
    private boolean hardcore;
    private GameMode gameMode;
    private int dimension;
    private Difficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;

    public WrappedPacketOutLogin(Object packet) {
        super(packet);
    }

    public WrappedPacketOutLogin() {
        super();
    }

    public WrappedPacketOutLogin(int playerId, boolean hardcore, GameMode gameMode, int dimension, Difficulty difficulty, int maxPlayers, WorldType worldType, boolean reducedDebugInfo) {
        super();
        this.playerId = playerId;
        this.hardcore = hardcore;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.worldType = worldType;
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public static void load() {

        PACKET_CLASS = PacketTypeClasses.Server.LOGIN;

        try {
            try {
                ENUM_GAMEMODE_CLASS = (Class<? extends Enum>) NMSUtils.getNMSClass("EnumGamemode");
            } catch (Throwable throwable) {
                ENUM_GAMEMODE_CLASS = (Class<? extends Enum>) NMSUtils.getNMSClass("WorldSettings$EnumGamemode");
            }

            ENUM_GAMEMODE_NOT_SET = Enum.valueOf(ENUM_GAMEMODE_CLASS, "NOT_SET");

            ENUM_DIFFICULTY_CLASS = (Class<? extends Enum>) NMSUtils.getNMSClass("EnumDifficulty");
            WORLD_TYPE_CLASS = NMSUtils.getNMSClass("WorldType");

            NMS_WORLD_TYPE_GET_BY_NAME = Reflection.getMethod(WORLD_TYPE_CLASS, WORLD_TYPE_CLASS, 0, String.class);
            NMS_WORLD_TYPE_NAME = Reflection.getMethod(WORLD_TYPE_CLASS, String.class, 0);

            PACKET_CONSTRUCTOR = PACKET_CLASS.getDeclaredConstructor(
                    int.class,
                    ENUM_GAMEMODE_CLASS,
                    boolean.class,
                    int.class,
                    ENUM_DIFFICULTY_CLASS,
                    int.class,
                    WORLD_TYPE_CLASS,
                    boolean.class
            );
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    protected void setup() {
        this.playerId = readInt(0);
        this.hardcore = readBoolean(0);
        Enum gameModeEnum = (Enum) readObject(0, ENUM_GAMEMODE_CLASS);
        if (gameModeEnum != null && !gameModeEnum.name().equals("NOT_SET")) {
            this.gameMode = GameMode.valueOf(gameModeEnum.name());
        }
        this.dimension = readInt(1);
        Enum difficultyEnum = (Enum) readObject(1, ENUM_DIFFICULTY_CLASS);
        if (difficultyEnum != null) {
            this.difficulty = Difficulty.valueOf(difficultyEnum.name());
        }
        Object worldType = readObject(0, WORLD_TYPE_CLASS);
        if (worldType != null) {
            try {
                this.worldType = WorldType.getByName((String) NMS_WORLD_TYPE_NAME.invoke(null, worldType));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        this.maxPlayers = readInt(2);
        this.reducedDebugInfo = readBoolean(0);
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setWorldType(WorldType worldType) {
        this.worldType = worldType;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getDimension() {
        return dimension;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public boolean isReducedDebugInfo() {
        return reducedDebugInfo;
    }

    @Override
    public Object asNMSPacket() {
        try {
            return PACKET_CONSTRUCTOR.newInstance(
                    this.playerId,
                    this.hardcore,
                    this.gameMode != null ? Enum.valueOf(ENUM_GAMEMODE_CLASS, this.gameMode.name()) : ENUM_GAMEMODE_NOT_SET,
                    this.dimension,
                    Enum.valueOf(ENUM_DIFFICULTY_CLASS, this.difficulty.name()),
                    this.maxPlayers,
                    NMS_WORLD_TYPE_GET_BY_NAME.invoke(null, this.worldType.getName()),
                    this.reducedDebugInfo
            );
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
