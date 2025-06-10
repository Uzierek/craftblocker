package com.example.craftingblocker;

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
            // Sprawdzamy czy gracz otwiera swój ekwipunek (typ PLAYER)
            if (event.getInventory().getType().toString().equals("PLAYER")) {
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
            // Sprawdzamy czy gracz zamyka swój ekwipunek (typ PLAYER)
            if (event.getInventory().getType().toString().equals("PLAYER")) {
                removeCraftingBlockers(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Sprawdzamy czy to kliknięcie w ekwipunku gracza (typ PLAYER)
            if (event.getInventory().getType().toString().equals("PLAYER")) {
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
                        if (item == null || item.getType() == Material.AIR) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "Nie możesz przenosić przedmiotów do pól craftingu!");
                            return;
                        }
                    }
                }
            }
        }
    }

    private void setCraftingBlockers(Player player) {
        // Ustawiamy blokery w slotach craftingu (1-4)
        // Używamy różnych bloków jak w twoim Skript
        ItemStack[] blockers = {
            createBlockerItem(Material.NETHERRACK),
            createBlockerItem(Material.STONE), 
            createBlockerItem(Material.WOOD),
            createBlockerItem(Material.COBBLESTONE)
        };
        
        for (int i = 1; i <= 4; i++) {
            ItemStack currentItem = player.getInventory().getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                player.getInventory().setItem(i, blockers[i-1]);
            }
        }
    }
    
    private ItemStack createBlockerItem(Material material) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Pole zablokowane");
        item.setItemMeta(meta);
        return item;
    }

    private void removeCraftingBlockers(Player player) {
        // Usuwamy blokery ze slotów craftingu (1-4)
        Material[] blockerTypes = {Material.NETHERRACK, Material.STONE, Material.WOOD, Material.COBBLESTONE};
        
        for (int i = 1; i <= 4; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isSpecificBlockerItem(item, blockerTypes[i-1])) {
                player.getInventory().setItem(i, null);
            }
        }
    }
    
    private boolean isSpecificBlockerItem(ItemStack item, Material expectedType) {
        if (item == null || item.getType() != expectedType) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(ChatColor.RED + "Pole zablokowane");
    }

    private boolean isBlockerItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        // Sprawdzamy czy to jeden z naszych blokerów
        Material[] blockerTypes = {Material.NETHERRACK, Material.STONE, Material.WOOD, Material.COBBLESTONE};
        boolean isBlockerType = false;
        
        for (Material type : blockerTypes) {
            if (item.getType() == type) {
                isBlockerType = true;
                break;
            }
        }
        
        if (!isBlockerType) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(ChatColor.RED + "Pole zablokowane");
    }
}
