package xyz.acrylicstyle.loreedittest.network;

import com.google.gson.JsonParseException;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.EnumChatFormat;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MerchantRecipe;
import net.minecraft.server.v1_15_R1.MerchantRecipeList;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutWindowItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PacketRewriter {
    public static List<Object> processIncomingPacket(@NotNull PacketData packetData) {
        Packet<?> packet = packetData.getPacket();
        if (packet instanceof PacketPlayInSetCreativeSlot) {
            reverseProcessItemStack(packetData.getField("b"));
        }
        return Collections.singletonList(packet);
    }

    public static List<Object> processOutgoingPacket(@NotNull PacketData packetData) {
        Packet<?> packet = packetData.getPacket();
        if (packet instanceof PacketPlayOutWindowItems) {
            for (ItemStack stack : packetData.<List<ItemStack>>getField("b")) {
                processItemStack(stack);
            }
        } else if (packet instanceof PacketPlayOutOpenWindowMerchant) {
            for (MerchantRecipe merchantRecipe : packetData.<MerchantRecipeList>getField("b")) {
                processItemStack(merchantRecipe.buyingItem1);
                processItemStack(merchantRecipe.buyingItem2);
                processItemStack(merchantRecipe.sellingItem);
            }
        } else if (packet instanceof PacketPlayOutEntityEquipment) {
            processItemStack(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutSetSlot) {
            processItemStack(packetData.getField("c"));
        }
        return Collections.singletonList(packet);
    }

    public static ItemStack processItemStack(@Nullable ItemStack item) {
        if (item == null) return null;
        if (!item.hasTag()) return item;
        NBTTagCompound tag = item.getOrCreateTag();
        if (tag.getBoolean("LoreEditTest.modifiedTag")) {
            // player has copied the item with creative mode or something
            return item;
        }
        NBTTagCompound displayTag = tag.getCompound("display");
        int lines = 0;
        if (displayTag != null && (displayTag.hasKeyOfType("Lore", 8) || displayTag.hasKeyOfType("Lore", 9))) {
            if (displayTag.hasKeyOfType("Lore", 8)) {
                try {
                    IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(displayTag.getString("Lore"));
                    if (component != null) {
                        String mmid = tag.hasKeyOfType("MYTHIC_TYPE", 8) ? tag.getString("MYTHIC_TYPE") : null;
                        if (mmid != null) {
                            component.addSibling(new ChatComponentText("MMID: " + mmid).a(cm -> cm.setItalic(false)).a(EnumChatFormat.DARK_GRAY));
                            lines++;
                        }
                        displayTag.setString("Lore", IChatBaseComponent.ChatSerializer.a(component));
                        tag.set("display", displayTag);
                    }
                } catch (JsonParseException ignored) {
                }
            } else {
                NBTTagList list = displayTag.getList("Lore", 8);
                String mmid = tag.hasKeyOfType("MYTHIC_TYPE", 8) ? tag.getString("MYTHIC_TYPE") : null;
                if (mmid != null) {
                    list.add(NBTTagString.a(IChatBaseComponent.ChatSerializer.a(new ChatComponentText("MMID: " + mmid).a(cm -> cm.setItalic(false)).a(EnumChatFormat.DARK_GRAY))));
                    lines++;
                }
            }
        }
        if (lines >= 1) {
            tag.setInt("LoreEditTest.modifiedTag", 1);
        }
        item.setTag(tag);
        return item;
    }

    public static ItemStack reverseProcessItemStack(@Nullable ItemStack item) {
        if (item == null) return null;
        if (!item.hasTag()) return item;
        NBTTagCompound tag = item.getOrCreateTag();
        if (!tag.hasKeyOfType("LoreEditTest.modifiedTag", 99)) {
            return item;
        }
        int count = tag.getInt("LoreEditTest.modifiedTag");
        NBTTagCompound displayTag = tag.getCompound("display");
        if (displayTag != null && (displayTag.hasKeyOfType("Lore", 8) || displayTag.hasKeyOfType("Lore", 9))) {
            if (displayTag.hasKeyOfType("Lore", 8)) {
                try {
                    IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(displayTag.getString("Lore"));
                    if (component != null) {
                        for (int i = 0; i < count; i++) {
                            component.getSiblings().remove(component.getSiblings().size() - 1);
                        }
                        displayTag.setString("Lore", IChatBaseComponent.ChatSerializer.a(component));
                        tag.set("display", displayTag);
                    }
                } catch (JsonParseException ignored) {
                }
            } else {
                NBTTagList list = displayTag.getList("Lore", 8);
                for (int i = 0; i < count; i++) {
                    list.remove(list.size() - 1);
                }
            }
        }
        tag.remove("LoreEditTest.modifiedTag");
        item.setTag(tag);
        return item;
    }
}
