package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * General event listener
 * This listener is active on all events
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public class GeneralEventListener implements Listener {
    private EventManager eventManager;

    public GeneralEventListener(EventsMain plugin) {
        this.eventManager = plugin.getManagerHandler().getEventManager();
    }

    private boolean runEvent(Player player) {
        return eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (runEvent(player)) {
            eventManager.leaveEvent(player);
        }
    }

    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        if (runEvent(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactNetherStar(PlayerInteractEvent event) {
        if (runEvent(event.getPlayer())) {
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
                return;

            Player player = event.getPlayer();
            if (item.getItemMeta().getDisplayName().contains("Leave Event") && eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player)) {
                eventManager.leaveEvent(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void itemCraft(CraftItemEvent event) {
        if (runEvent((Player) event.getWhoClicked())) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(EventMessage.CRAFT_IN_EVENT.toString());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (runEvent(event.getEntity())) {
            event.setDeathMessage("");
        }
    }
}
