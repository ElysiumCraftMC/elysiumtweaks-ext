package codes.dreaming.elysiumtweaksExt.packets;

import codes.dreaming.elysiumtweaksExt.utils.FriendlyByteBuf;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.netty.buffer.Unpooled;

import static codes.dreaming.elysiumtweaksExt.ElysiumtweaksExt.COMPANION_MOD_ID;

enum QueryNickPacketState {
    check,
    choose
}

public class QueryNickPacket extends FriendlyByteBuf {
    public static ChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.create(COMPANION_MOD_ID, "query_nick");

    QueryNickPacket(QueryNickPacketState state, boolean status) {
        super(Unpooled.buffer());

        this.writeUtf(state.name());
        this.writeBoolean(status);
    }

    public static QueryNickPacket closePacket() {
        return new QueryNickPacket(QueryNickPacketState.choose, true);
    }

    public static QueryNickPacket invalidPacket() {
        return new QueryNickPacket(QueryNickPacketState.choose, false);
    }

    public static QueryNickPacket openPacket() {
        return new QueryNickPacket(QueryNickPacketState.check, false);
    }

    public void sendToPlayer(Player player) {
        player.sendPluginMessage(IDENTIFIER, this.array());
    }
}
