package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * OITC event:
 * All players have a wooden sword, a bow, and 1 arrow
 * Arrows one shot and when you get a kill you receive an arrow
 *
 * @author Nicbo
 * @since 2020-03-13
 */

public class OITC extends InvadedEvent {
    private List<Location> locations;
    private List<ItemStack> kit;

    private HashMap<Player, Integer> points;
    private Set<Player> respawningPlayers;

    public OITC(EventsMain plugin) {
        super("One in the Chamber", "oitc", plugin);
        this.locations = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.kit = Arrays.asList(new ItemStack(Material.WOOD_SWORD, 1), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 1));
        this.points = new HashMap<>();
        this.respawningPlayers = new HashSet<>();
    }

    @Override
    public void init(EventsMain plugin) {
        initPlayerCheck();
    }

    @Override
    public void start() {
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        for (Player player : players) {
            points.put(player, 0);
            preparePlayer(player);
            player.teleport(getRandomLocation());
        }
    }

    @Override
    public void stop() {
        started = false;
        playerCheck.cancel();
        removePlayers();
        respawningPlayers.clear();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    private void preparePlayer(Player player) {
        player.getInventory().clear();
        kit.forEach(item -> player.getInventory().addItem(item));
    }

    private Location getRandomLocation() {
        return locations.get(GeneralUtils.randomMinMax(0, 7));
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (event.getDamager() instanceof Arrow) {
                event.setDamage(20);
            }

            if (event.getDamage() >= player.getHealth()) {
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.spigot().respawn();
                    }
                }, 1);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!respawningPlayers.contains(player))
            return;

        Player killer = player.getKiller();
        if (killer != null && player != killer) {
            points.put(killer, points.get(killer) + 1);
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + "[" + points.get(player) + "]" + " has been killed by " + killer.getName() + "[" + points.get(killer) + "]");
            killer.setHealth(20);
            killer.getInventory().addItem(kit.get(2));
            if (points.get(killer) == 20)
                playerWon(killer);
        }

        preparePlayer(player);
        event.setRespawnLocation(getRandomLocation());
        respawningPlayers.remove(player);
    }

    /*
    TODO:
        - Make point cap configurable (20)
     */

}
