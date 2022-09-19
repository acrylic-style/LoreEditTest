package xyz.acrylicstyle.loreedittest.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.loreedittest.LoreEditTest;
import xyz.acrylicstyle.loreedittest.util.LoggedPrintStream;

public class ChannelHandler extends ChannelDuplexHandler {
    private final Player player;

    public ChannelHandler(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet<?>) {
            try {
                for (Object p : PacketRewriter.processIncomingPacket(new PacketData(player, msg))) {
                    super.channelRead(ctx, p);
                }
            } catch (Throwable e) {
                if (e instanceof VirtualMachineError) {
                    throw e;
                }
                LoreEditTest.getInstance().getLogger().severe("Exception while processing packet from " + player.getName());
                e.printStackTrace(new LoggedPrintStream(LoreEditTest.getInstance().getLogger(), System.err));
                throw e;
            }
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet<?>) {
            try {
                for (Object p : PacketRewriter.processOutgoingPacket(new PacketData(player, msg))) {
                    super.write(ctx, p, promise);
                }
            } catch (Throwable e) {
                if (e instanceof VirtualMachineError) {
                    throw e;
                }
                LoreEditTest.getInstance().getLogger().severe("Exception while processing packet to " + player.getName());
                e.printStackTrace(new LoggedPrintStream(LoreEditTest.getInstance().getLogger(), System.err));
                throw e;
            }
            return;
        }
        super.write(ctx, msg, promise);
    }
}
