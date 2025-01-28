package codes.dreaming.elysiumtweaksExt;

import codes.dreaming.elysiumtweaksExt.packets.QueryNickPacket;
import codes.dreaming.elysiumtweaksExt.utils.ConfigHelper;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Logger;

@Plugin(
        id = ElysiumtweaksExt.PLUGIN_ID,
        name = "elysiumtweaks-ext",
        version = BuildConstants.VERSION,
        url = "https://dreaming.codes",
        authors = {"DreamingCodes"},
        dependencies = {
                @Dependency(id = "dreamingqueue")
        }
)
public class ElysiumtweaksExt {
    public static final String PLUGIN_ID = "elysiumtweaks-ext";
    public static final String COMPANION_MOD_ID = "elysium";

    private final ConfigHelper configHelper;
    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    ElysiumtweaksExt(Logger logger, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.logger = logger;

        this.configHelper = new ConfigHelper(logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws SerializationException {
        this.configHelper.loadConfiguration();
        proxyServer.getChannelRegistrar().register(QueryNickPacket.IDENTIFIER);

        RegisteredServer changeNickServer = proxyServer.getServer(configHelper.getChangeNickServer()).orElseThrow();

        proxyServer.getEventManager().register(this, new ElysiumTweaksExtEventHandler(logger, configHelper, changeNickServer));
    }
}
