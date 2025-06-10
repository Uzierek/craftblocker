package pl.twojastrona.craftingblocker;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

public class CraftingBlocker extends JavaPlugin implements Listener {

    private ItemStack blockerItem;

    @Override
    public void onEnable() {
        // Rejestrujemy event listener
        getServer().getPluginManager().registerEvents(this, this);
        
        // Tworzymy item blokujący (biała wełna z nazwą)
        blockerItem = new ItemStack(Material.WOOL, 1, (short) 0); // Biała wełna
        ItemMeta meta = blockerItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Pole zablokowane");
        blockerItem.setItemMeta(meta);
        
        getLogger().info("CraftingBlocker został włączony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CraftingBlocker został wyłączony!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Ustawiamy blokery przy dołączeniu gracza
        final Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                setCraftingBlockers(player);
            }
        }.runTaskLater(this, 10L); // Czekamy 0.5 sekundy
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // Sprawdzamy czy gracz otwiera swój ekwipunek
            if (event.getInventory().equals(player.getInventory())) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        setCraftingBlockers(player);
                    }
                }.runTaskLater(this, 1L);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // Sprawdzamy czy gracz zamyka swój ekwipunek
            if (event.getInventory().equals(player.getInventory())) {
                removeCraftingBlockers(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Sprawdzamy czy to kliknięcie w ekwipunku gracza
            if (event.getInventory().equals(player.getInventory())) {
                int slot = event.getRawSlot();
                
                // Sloty craftingu to 1, 2, 3, 4 w ekwipunku gracza
                if (slot >= 1 && slot <= 4) {
                    ItemStack clickedItem = event.getCurrentItem();
                    
                    // Sprawdzamy czy to nasz bloker
                    if (isBlockerItem(clickedItem)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "To pole craftingu jest zablokowane!");
                        return;
                    }
                }
                
                // Blokujemy shift+click do pól craftingu
                if (event.isShiftClick() && event.getRawSlot() >= 9) {
                    for (int i = 1; i <= 4; i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (isBlockerItem(item)) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "Nie możesz przenosić przedmiotów - pola craftingu są zablokowane!");
                            return;
                        }
                    }
                }
            }
        }
    }

    private void setCraftingBlockers(Player player) {
        // Ustawiamy blokery w slotach craftingu (1-4)
        for (int i = 1; i <= 4; i++) {
            ItemStack currentItem = player.getInventory().getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                player.getInventory().setItem(i, blockerItem.clone());
            }
        }
    }

    private void removeCraftingBlockers(Player player) {
        // Usuwamy blokery ze slotów craftingu (1-4)
        for (int i = 1; i <= 4; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isBlockerItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    private boolean isBlockerItem(ItemStack item) {
        if (item == null || item.getType() != Material.WOOL) {
            return false;
        }
        
        if (item.getDurability() != 0) { // Sprawdzamy czy to biała wełna
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(ChatColor.RED + "Pole zablokowane");
    }
}
