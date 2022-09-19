package xyz.acrylicstyle.loreedittest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.loreedittest.listener.JoinQuitListener;
import xyz.acrylicstyle.loreedittest.util.PacketUtil;

import java.util.Objects;

public final class LoreEditTest extends JavaPlugin {
    private static LoreEditTest instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
        Bukkit.getOnlinePlayers().forEach(PacketUtil::inject);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(PacketUtil::eject);
    }

    @NotNull
    public static LoreEditTest getInstance() {
        return Objects.requireNonNull(instance, "plugin is not enabled yet");
    }
}
