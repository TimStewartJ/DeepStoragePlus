package me.darkolythe.deepstorageplus.dsu.listeners;

import me.darkolythe.deepstorageplus.DeepStoragePlus;
import me.darkolythe.deepstorageplus.utils.ItemList;
import me.darkolythe.deepstorageplus.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Optional;

public class WrenchListener implements Listener {

    private DeepStoragePlus main;
    public WrenchListener(DeepStoragePlus plugin) {
        main = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onWrenchUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack storageWrench = ItemList.createStorageWrench();
            ItemStack sorterWrench = ItemList.createSorterWrench();
            ItemStack accessorWrench = ItemList.createAccessorWrench();
            ItemStack linkModule = ItemList.createLinkModule();

            Block block = event.getClickedBlock();
            if (player.getInventory().getItemInMainHand().equals(storageWrench)
                    || player.getInventory().getItemInMainHand().equals(sorterWrench) || player.getInventory().getItemInMainHand().equals(accessorWrench)) {
                if (block != null && block.getType() == Material.CHEST) {
                    if (player.hasPermission("deepstorageplus.create")) {
                        if (!event.isCancelled()) {
                            event.setCancelled(true);
                            if (isInventoryEmpty(block)) {
                                if (sizeOfInventory(block) == 54) {
                                    if (player.getInventory().getItemInMainHand().equals(storageWrench)) {
                                        createUnit(block, "DSU");
                                        player.getInventory().getItemInMainHand().setAmount(0);
                                        player.sendMessage(DeepStoragePlus.prefix + ChatColor.GREEN + LanguageManager.getValue("dsucreate"));
                                    } else if (player.getInventory().getItemInMainHand().equals(sorterWrench)) {
                                        createUnit(block, "sorter");
                                        player.getInventory().getItemInMainHand().setAmount(0);
                                        player.sendMessage(DeepStoragePlus.prefix + ChatColor.GREEN + LanguageManager.getValue("sortercreate"));
                                    } else if (player.getInventory().getItemInMainHand().equals(accessorWrench)) {
                                        createUnit(block, "accessor");
                                        player.getInventory().getItemInMainHand().setAmount(0);
                                        player.sendMessage(DeepStoragePlus.prefix + ChatColor.GREEN + LanguageManager.getValue("sortercreate"));
                                    }
                                } else {
                                    player.sendMessage(DeepStoragePlus.prefix + ChatColor.RED + LanguageManager.getValue("chestmustbedouble"));
                                }
                            } else {
                                player.sendMessage(DeepStoragePlus.prefix + ChatColor.RED + LanguageManager.getValue("chestmustbeempty"));
                            }
                        }
                    } else {
                        player.sendMessage(DeepStoragePlus.prefix + ChatColor.RED + LanguageManager.getValue("nopermission"));
                    }
                } else if (block != null && block.getType() == Material.GRASS_BLOCK) { // Handle using the "shovel" to make dirt paths
                    event.setCancelled(true);
                }
            }


            if (ItemList.compareItem(player.getInventory().getItemInMainHand(), linkModule)) {
                if (block != null && block.getType() == Material.CHEST) {
                    if (!event.isCancelled()) {
                        event.setCancelled(true);
                        if (isUnit(block)) {
                            ItemMeta linkModuleMeta = player.getInventory().getItemInMainHand().getItemMeta();
                            linkModuleMeta.setLore(Arrays.asList(ChatColor.BLUE + String.format("%s %s %s %s", block.getWorld().getName(), block.getX(), block.getY(), block.getZ())));
                            player.getInventory().getItemInMainHand().setItemMeta(linkModuleMeta);
                            player.sendMessage(DeepStoragePlus.prefix + ChatColor.GREEN + "DSU Coordinates Saved");
                        } else {
                            player.sendMessage(DeepStoragePlus.prefix + ChatColor.RED + "This is not a DSU");
                        }
                    }
                } else if (block != null && block.getType() == Material.GRASS_BLOCK) { // Handle using the "shovel" to make dirt paths
                    event.setCancelled(true);
                }
            }
        }
    }

    private static boolean isInventoryEmpty(Block block) {
        Inventory inv = getInventoryFromBlock(block);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null) {
                return false;
            }
        }
        return true;
    }

    private static int sizeOfInventory(Block block) {
        Inventory inv = getInventoryFromBlock(block);
        return inv.getSize();
    }

    private void createUnit(Block block, String type) {
        String name = "";
        switch(type) {
            case "DSU":
                name = DeepStoragePlus.DSUname;
                break;
            case "sorter":
                name = DeepStoragePlus.sortername;
                break;
            case "accessor":
                name = DeepStoragePlus.accessorname;
                break;
            default:
                break;
        }
        Chest chest = (Chest) block.getState();
        chest.setCustomName(name);
        chest.update();
    }

    private boolean isUnit(Block block) {
        return isUnit(block, DeepStoragePlus.DSUname) || isUnit(block, DeepStoragePlus.accessorname) || isUnit(block, DeepStoragePlus.sortername);
    }

    private boolean isUnit(Block block, String type) {
        Chest chest = (Chest) block.getState();
        if (chest.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getRightSide();
            return (leftChest.getCustomName() != null && leftChest.getCustomName().equals(type)) ||
                    (rightChest.getCustomName() != null && rightChest.getCustomName().equals(type));
        }
        return chest.getCustomName() != null && chest.getCustomName().equals(type);
    }

    private static Inventory getInventoryFromBlock(Block block) {
        BlockState bs = block.getState();
        Chest chest = (Chest) bs;
        return chest.getInventory();
    }
}
