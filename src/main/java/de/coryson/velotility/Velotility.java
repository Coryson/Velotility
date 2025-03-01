package de.coryson.velotility;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(id = "velotility", name = "Velotility", version = "1.0", description = "A simple plugin to limit the amount of connections to the server.")
public class Velotility {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    private final Map<Player, Long> loginTimes = new ConcurrentHashMap<>();


    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        int totalPlayers = server.getAllServers().stream()
                .mapToInt(s -> s.getPlayersConnected().size())
                .sum();

        int maxConnections = 1;
        if (!player.hasPermission("velotility.bypass") && totalPlayers >= maxConnections) {
            player.disconnect(Component.text("Server is full. Please try again later."));
        } else {
            loginTimes.put(player, System.currentTimeMillis());
            Component joinMessage = Component.text("Player " + player.getUsername() + " has joined the server.");
            server.getAllPlayers().forEach(p -> p.sendMessage(joinMessage));
            logger.info("Player {} connected. Total players: {}", player.getUsername(), totalPlayers + 1);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (loginTimes.containsKey(player)) {
            Component leaveMessage = Component.text("Player " + player.getUsername() + " has left the server.");
            server.getAllPlayers().forEach(p -> p.sendMessage(leaveMessage));
            logger.info("Player {} disconnected. Total players: {}", player.getUsername(), server.getAllServers().stream()
                    .mapToInt(s -> s.getPlayersConnected().size())
                    .sum());
            loginTimes.remove(player);
        }
    }
}