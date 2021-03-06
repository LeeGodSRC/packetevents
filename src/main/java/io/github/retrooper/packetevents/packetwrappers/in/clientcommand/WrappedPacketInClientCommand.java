/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.packetwrappers.in.clientcommand;

import io.github.retrooper.packetevents.packet.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;

public final class WrappedPacketInClientCommand extends WrappedPacket {
    private static Class<?> packetClass;
    private static Class<?> enumClientCommandClass;
    private ClientCommand clientCommand;

    public WrappedPacketInClientCommand(Object packet) {
        super(packet);
    }

    public static void load() {
        packetClass = PacketTypeClasses.Client.CLIENT_COMMAND;

        try {
            enumClientCommandClass = NMSUtils.getNMSClass("EnumClientCommand");
        } catch (ClassNotFoundException e) {
            //Probably a subclass
            enumClientCommandClass = SubclassUtil.getSubClass(packetClass, "EnumClientCommand");
        }
    }

    @Override
    public void setup() {
        Object enumObj = readObject(0, enumClientCommandClass);
        this.clientCommand = ClientCommand.valueOf(enumObj.toString());
    }

    /**
     * Get the Client Command enum sent in the packet
     *
     * @return ClientCommand
     */
    public ClientCommand getClientCommand() {
        return clientCommand;
    }

    public enum ClientCommand {
        PERFORM_RESPAWN,
        REQUEST_STATS,
        OPEN_INVENTORY_ACHIEVEMENT
    }

}
