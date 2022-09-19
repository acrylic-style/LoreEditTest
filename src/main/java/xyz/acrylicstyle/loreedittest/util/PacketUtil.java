package xyz.acrylicstyle.loreedittest.util;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.loreedittest.LoreEditTest;
import xyz.acrylicstyle.loreedittest.network.ChannelHandler;

import java.util.NoSuchElementException;

public class PacketUtil {
    private static final String NAME = "loreedittest";

    public static void inject(@NotNull Player player) {
        ChannelHandler handler = new ChannelHandler(player);
        try {
            Util.getChannel(player).pipeline().addBefore("packet_handler", NAME, handler);
            LoreEditTest.getInstance().getLogger().info("Injected packet handler for " + player.getName());
        } catch (NoSuchElementException ex) {
            Bukkit.getScheduler().runTaskLater(LoreEditTest.getInstance(), () -> {
                if (!player.isOnline()) return;
                try {
                    Util.getChannel(player).pipeline().addBefore("packet_handler", NAME, handler);
                    LoreEditTest.getInstance().getLogger().info("Injected packet handler for " + player.getName());
                } catch (NoSuchElementException ignore) {
                    LoreEditTest.getInstance().getLogger().warning("Failed to inject packet handler to " + player.getName());
                }
            }, 10);
        }
    }

    public static void eject(@NotNull Player player) {
        try {
            if (Util.getChannel(player).pipeline().get(NAME) != null) {
                Util.getChannel(player).pipeline().remove(NAME);
                LoreEditTest.getInstance().getLogger().info("Ejected packet handler from " + player.getName());
            }
        } catch (RuntimeException e) {
            LoreEditTest.getInstance().getLogger().info("Failed to eject packet handler from " + player.getName() + ", are they already disconnected?");
        }
    }
}
