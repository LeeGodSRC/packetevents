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

package io.github.retrooper.packetevents.packetwrappers.in.windowclick;

import io.github.retrooper.packetevents.packet.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class WrappedPacketInWindowClick extends WrappedPacket {
    private static final HashMap<String, Integer> invClickTypeMapCache = new HashMap<String, Integer>();
    private static final HashMap<Integer, ArrayList<WindowClickType>> windowClickTypeCache = new HashMap<Integer, ArrayList<WindowClickType>>();
    private static Class<?> packetClass, invClickTypeClass;
    private static boolean isClickModePrimitive = false;
    private int id;
    private int slot;
    private int button;
    private short actionNumber;
    private int mode;
    private ItemStack clickedItem;

    public WrappedPacketInWindowClick(Object packet) {
        super(packet);
    }

    public static void load() {
        packetClass = PacketTypeClasses.Client.WINDOW_CLICK;
        invClickTypeClass = NMSUtils.getNMSClassWithoutException("InventoryClickType");

        invClickTypeMapCache.put("PICKUP", 0);
        invClickTypeMapCache.put("QUICK_MOVE", 1);
        invClickTypeMapCache.put("SWAP", 2);
        invClickTypeMapCache.put("CLONE", 3);
        invClickTypeMapCache.put("THROW", 4);
        invClickTypeMapCache.put("QUICK_CRAFT", 5);
        invClickTypeMapCache.put("PICKUP_ALL", 6);

        //MODE 0
        windowClickTypeCache.put(0, getArrayListOfWindowClickTypes(WindowClickType.LEFT_MOUSE_CLICK,
                WindowClickType.RIGHT_MOUSE_CLICK));

        //MODE 1
        windowClickTypeCache.put(1, getArrayListOfWindowClickTypes(WindowClickType.SHIFT_LEFT_MOUSE_CLICK,
                WindowClickType.SHIFT_RIGHT_MOUSE_CLICK));

        //MODE 2
        windowClickTypeCache.put(2, getArrayListOfWindowClickTypes(
                WindowClickType.KEY_NUMBER1,
                WindowClickType.KEY_NUMBER2,
                WindowClickType.KEY_NUMBER3,
                WindowClickType.KEY_NUMBER4,
                WindowClickType.KEY_NUMBER5,
                WindowClickType.KEY_NUMBER6,
                WindowClickType.KEY_NUMBER7,
                WindowClickType.KEY_NUMBER8,
                WindowClickType.KEY_NUMBER9));

        //MODE 3
        windowClickTypeCache.put(3, getArrayListOfWindowClickTypes(WindowClickType.UNKNOWN, WindowClickType.UNKNOWN, WindowClickType.CREATIVE_MIDDLE_CLICK));

        //MODE 4
        windowClickTypeCache.put(4, getArrayListOfWindowClickTypes(WindowClickType.KEY_DROP,
                WindowClickType.KEY_DROP_STACK));

        //MODE 5
        windowClickTypeCache.put(5, getArrayListOfWindowClickTypes(
                WindowClickType.STARTING_LEFT_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_LEFT_MOUSE_DRAG,
                WindowClickType.ENDING_LEFT_MOUSE_DRAG,
                WindowClickType.UNKNOWN,
                WindowClickType.STARTING_RIGHT_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_RIGHT_MOUSE_DRAG,
                WindowClickType.CREATIVE_STARTING_MIDDLE_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_MIDDLE_MOUSE_DRAG,
                WindowClickType.ENDING_MIDDLE_MOUSE_DRAG));

        windowClickTypeCache.put(6, getArrayListOfWindowClickTypes(WindowClickType.DOUBLE_CLICK));
        isClickModePrimitive = Reflection.getField(packetClass, int.class, 3) != null;
    }

    private static ArrayList<WindowClickType> getArrayListOfWindowClickTypes(WindowClickType... types) {
        ArrayList<WindowClickType> arrayList = new ArrayList<WindowClickType>(types.length);
        arrayList.addAll(Arrays.asList(types));
        return arrayList;
    }

    @Override
    protected void setup() {
        this.id = readInt(0);
        this.slot = readInt(1);
        this.button = readInt(2);
        this.actionNumber = readShort(0);
        Object nmsItemStack = readObject(0, NMSUtils.nmsItemStackClass);
        this.clickedItem = NMSUtils.toBukkitItemStack(nmsItemStack);
        Object clickMode = readAnyObject(5);

        if (isClickModePrimitive) {
            mode = (int) clickMode;
        } else {
            mode = invClickTypeMapCache.get(clickMode.toString());
        }
    }

    /**
     * Get the Window ID.
     * @return Get Window ID
     */
    public int getWindowID() {
        return id;
    }

    /**
     * Get the Window slot.
     * @return Get Window Slot
     */
    public int getWindowSlot() {
        return slot;
    }

    /**
     * Get the Window button.
     * @return Get Window Button
     */
    public int getWindowButton() {
        return button;
    }

    /**
     * Get the action number.
     * @return Get Action Number
     */
    public short getActionNumber() {
        return actionNumber;
    }

    /**
     * Get the window click type.
     * @return Get Window Click Type
     */
    public WindowClickType getWindowClickType() {
        if (windowClickTypeCache.get(mode) == null) {
            return WindowClickType.UNKNOWN;
        }
        if (button + 1 > windowClickTypeCache.size()) {
            return WindowClickType.UNKNOWN;
        }

        if (mode == 4) {
            if (slot == -999) {
                if (button == 0) {
                    return WindowClickType.LEFT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING;
                } else if (button == 1) {
                    return WindowClickType.RIGHT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING;
                }
            }
        }
        return windowClickTypeCache.get(mode).get(button);
    }

    /**
     * Get the Window mode.
     * @return Get Window Mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get the clicked item.
     * @return Get Clicked ItemStack
     */
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    public enum WindowClickType {
        LEFT_MOUSE_CLICK, RIGHT_MOUSE_CLICK,
        SHIFT_LEFT_MOUSE_CLICK, SHIFT_RIGHT_MOUSE_CLICK,

        CREATIVE_MIDDLE_CLICK, CREATIVE_STARTING_MIDDLE_MOUSE_DRAG,

        KEY_NUMBER1, KEY_NUMBER2, KEY_NUMBER3, KEY_NUMBER4,
        KEY_NUMBER5, KEY_NUMBER6, KEY_NUMBER7, KEY_NUMBER8,
        KEY_NUMBER9, KEY_DROP, KEY_DROP_STACK,

        LEFT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING,
        RIGHT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING,

        STARTING_LEFT_MOUSE_DRAG,
        STARTING_RIGHT_MOUSE_DRAG,

        ADD_SLOT_LEFT_MOUSE_DRAG,
        ADD_SLOT_RIGHT_MOUSE_DRAG,
        ADD_SLOT_MIDDLE_MOUSE_DRAG,

        ENDING_LEFT_MOUSE_DRAG,
        ENDING_RIGHT_MOUSE_DRAG,
        ENDING_MIDDLE_MOUSE_DRAG,

        DOUBLE_CLICK,

        UNKNOWN
    }
}
