/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.packetwrappers.play.in.custompayload;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class WrappedPacketInCustomPayload extends WrappedPacket {
    private static boolean strPresent, byteArrayPresent, customPacketPayloadPresent;
    private static Class<?> CUSTOM_PACKET_PAYLOAD;
    private static Method CUSTOM_PACKET_PAYLOAD_MINECRAFT_KEY, CUSTOM_PACKET_PAYLOAD_PACKETDATASERIALIZER;
    private static byte isVersion_1_17 = -1;

    public WrappedPacketInCustomPayload(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        strPresent = Reflection.getField(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, String.class, 0) != null;
        byteArrayPresent = Reflection.getField(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, byte[].class, 0) != null;
        CUSTOM_PACKET_PAYLOAD = Reflection.getClassByNameWithoutException("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
        customPacketPayloadPresent = Reflection.getField(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, CUSTOM_PACKET_PAYLOAD, 0) != null;
        if (customPacketPayloadPresent) {
            CUSTOM_PACKET_PAYLOAD_MINECRAFT_KEY = Reflection.getMethod(CUSTOM_PACKET_PAYLOAD, NMSUtils.minecraftKeyClass, 0);
            CUSTOM_PACKET_PAYLOAD_PACKETDATASERIALIZER = Reflection.getMethod(CUSTOM_PACKET_PAYLOAD, 0, NMSUtils.packetDataSerializerClass);
        }
    }

    private Object getModernPayloadObject() {
        return readObject(0, CUSTOM_PACKET_PAYLOAD);
    }

    public String getChannelName() {
        if (customPacketPayloadPresent) {
            Object payload = getModernPayloadObject();
            try {
                if (isVersion_1_17 == -1) {
                    isVersion_1_17 = (byte) (version.isNewerThanOrEquals(ServerVersion.v_1_17) ? 1 : 0);
                }
                int namespaceIndex = isVersion_1_17 == 1 ? 2 : 0;
                int keyIndex = isVersion_1_17 == 1 ? 3 : 1;
                Object minecraftKey = CUSTOM_PACKET_PAYLOAD_MINECRAFT_KEY.invoke(payload);
                WrappedPacket minecraftKeyWrapper = new WrappedPacket(new NMSPacket(minecraftKey));
                return minecraftKeyWrapper.readString(namespaceIndex) + ":" + minecraftKeyWrapper.readString(keyIndex);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        } else if (strPresent) {
            return readString(0);
        } else {
            return readMinecraftKey(1);
        }
    }

    public void setChannelName(String channelName) {
        if (customPacketPayloadPresent) {
            throw new UnsupportedOperationException("PacketEvents v1.8 is a dying version, setting the channel name is not supported!");
        }
        else if (strPresent) {
            writeString(0, channelName);
        } else {
            writeMinecraftKey(1, channelName);
        }
    }

    public byte[] getData() {
        if (customPacketPayloadPresent) {
            Object payload = getModernPayloadObject();
            Object buffer = PacketEvents.get().getByteBufUtil().buffer();
            Object packetDataSerializer = NMSUtils.generatePacketDataSerializer(buffer);
            try {
                CUSTOM_PACKET_PAYLOAD_PACKETDATASERIALIZER.invoke(payload, packetDataSerializer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            byte[] data = PacketEvents.get().getByteBufUtil().getBytes(buffer);
            PacketEvents.get().getByteBufUtil().release(buffer);
            return data;
        }
        else if (byteArrayPresent) {
            return readByteArray(0);
        } else {
            return PacketEvents.get().getByteBufUtil().getBytes(getBuffer());
        }
    }

    public void setData(byte[] data) {
        if (byteArrayPresent) {
            writeByteArray(0, data);
        } else {
            PacketEvents.get().getByteBufUtil().setBytes(getBuffer(), data);
        }
    }

    private Object getBuffer() {
        Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
        WrappedPacket dataSerializerWrapper = new WrappedPacket(new NMSPacket(dataSerializer));

        return dataSerializerWrapper.readObject(0, NMSUtils.byteBufClass);
    }

    public void retain() {
        if (packet != null && !byteArrayPresent) {
            PacketEvents.get().getByteBufUtil().retain(getBuffer());
        }
    }

    public void release() {
        if (packet != null && !byteArrayPresent) {
            PacketEvents.get().getByteBufUtil().release(getBuffer());
        }
    }

}
