package codes.dreaming.elysiumtweaksExt;

import codes.dreaming.dreamingQueue.DreamingQueue;
import codes.dreaming.elysiumtweaksExt.packets.QueryNickPacket;
import codes.dreaming.elysiumtweaksExt.utils.ConfigHelper;
import codes.dreaming.elysiumtweaksExt.utils.FriendlyByteBuf;
import codes.dreaming.elysiumtweaksExt.utils.NickUtils;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.netty.buffer.Unpooled;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Logger;

public class ElysiumTweaksExtEventHandler {
    private final Logger logger;
    private final ConfigHelper configHelper;
    private final RegisteredServer changeNickServer;

    ElysiumTweaksExtEventHandler(Logger logger, ConfigHelper configHelper, RegisteredServer changeNickServer) {
        this.logger = logger;
        this.configHelper = configHelper;
        this.changeNickServer = changeNickServer;
    }

    @Subscribe(priority = 1000)
    public void onPlayerEnter(PlayerChooseInitialServerEvent event) {
        String nickname = NickUtils.getLpNickname(event.getPlayer().getUniqueId());
        if (nickname != null) return;

        // Send the player to a server while I wait for him to set the nickname
        event.setInitialServer(this.changeNickServer);
        DreamingQueue.skipPlayer(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) throws SerializationException {
        // Check if the identifier matches first, no matter the source.
        if (!QueryNickPacket.IDENTIFIER.equals(event.getIdentifier())) {
            return;
        }

        // mark PluginMessage as handled, indicating that the contents
        // should not be forwarding to their original destination.
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        // only attempt parsing the data if the source is a player
        if (!(event.getSource() instanceof Player player)) {
            return;
        }

        FriendlyByteBuf in = new FriendlyByteBuf(Unpooled.wrappedBuffer(event.getData()));

        String state = in.readUtf();

        switch (state) {
            case "check": {
                String nickname = NickUtils.getLpNickname(player.getUniqueId());
                if (nickname != null) return;

                QueryNickPacket.openPacket().sendToPlayer(player);
                break;
            }
            case "choose": {
                String userInput = in.readUtf();
                String parsedNickname = NickUtils.parseNickname(userInput);

                if (parsedNickname == null
                        || configHelper.getBlacklistedNicks().contains(parsedNickname)
                        || configHelper.getBlacklistedNickParts().parallelStream().anyMatch(parsedNickname::contains)
                ) {
                    QueryNickPacket.invalidPacket().sendToPlayer(player);
                    return;
                }

                LuckPerms lpProvider = LuckPermsProvider.get();
                UserManager lpUserManager = lpProvider.getUserManager();
                User lpUser = lpUserManager.getUser(player.getUniqueId());
                // lpUser can't be null since the player is in the server
                assert lpUser != null;

                // Check if the player already has a nickname, this shouldn't be possible since that screen only opens
                // for players without one, but we check just in case
                String existingNickname = NickUtils.getLpNickname(lpUser);
                if (existingNickname != null) {
                    DreamingQueue.requeuePlayer(player);
                    QueryNickPacket.closePacket().sendToPlayer(player);
                    return;
                }

                MetaNode nicknameNode = NickUtils.buildNicknameMetaNode(parsedNickname);

                // Check if there is any existing player with that same nickname
                var usernameMatches = lpUserManager.searchAll(NodeMatcher.equals(nicknameNode, NodeEqualityPredicate.EXACT)).join();
                if (!usernameMatches.isEmpty()) {
                    QueryNickPacket.invalidPacket().sendToPlayer(player);
                    return;
                }

                lpUser.data().add(nicknameNode);
                lpUserManager.saveUser(lpUser);
                DreamingQueue.requeuePlayer(player);
                QueryNickPacket.closePacket().sendToPlayer(player);
            }
        }
    }
}
