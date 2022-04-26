package me.acclashcorporation.crucials.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BanMenuUtils {

    public static void openBanMenu(Player p) {
        ArrayList<Player> list = new ArrayList<>(p.getServer().getOnlinePlayers());
        Inventory bangui = Bukkit.createInventory(p, 54, ChatColor.BLUE + "Player List");

        for (Player player : list) {

            ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta meta = playerhead.getItemMeta();

            meta.setDisplayName(player.getDisplayName());
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Player Health: " + ChatColor.RED + player.getHealth());
            lore.add(ChatColor.GOLD + "EXP: " + ChatColor.AQUA + player.getExp());
            meta.setLore(lore);
            playerhead.setItemMeta(meta);

            bangui.addItem(playerhead);

        }
        p.openInventory(bangui);
    }

    public static void openConfirmBanMenu(Player player, Player whotoban) {
        Inventory confirmbanmenu = Bukkit.createInventory(player, 54, ChatColor.RED + "Ban this player?");

        ItemStack ban = new ItemStack(Material.COCOA_BEANS, 1);
        ItemMeta banmeta = ban.getItemMeta();
        banmeta.setDisplayName(ChatColor.GREEN + "Ban Player");
        ban.setItemMeta(banmeta);
        confirmbanmenu.setItem(4, ban);

        ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta playermeta = playerhead.getItemMeta();
        playermeta.setDisplayName(whotoban.getDisplayName());
        playerhead.setItemMeta(playermeta);
        confirmbanmenu.setItem(14, playerhead);

        ItemStack cancel = new ItemStack(Material.BARRIER, 1);
        ItemMeta cancelmeta = cancel.getItemMeta();
        cancelmeta.setDisplayName(ChatColor.RED + "Cancel Ban");
        cancel.setItemMeta(cancelmeta);
        confirmbanmenu.setItem(16, cancel);

        player.openInventory(confirmbanmenu);
    }

    public static void openUnBanMenu(Player player, Player whotounban) {
        Inventory unbanmenu = Bukkit.createInventory(player, 54, ChatColor.RED + "Unban this player?");

        ItemStack ban = new ItemStack(Material.COCOA_BEANS, 1);
        ItemMeta banmeta = ban.getItemMeta();
        banmeta.setDisplayName(ChatColor.GREEN + "Unban Player");
        ArrayList<String> banlore = new ArrayList<>();
        banlore.add(ChatColor.GOLD + "This player is already banned. Would you like to unban them?");
        banmeta.setLore(banlore);
        ban.setItemMeta(banmeta);
        unbanmenu.setItem(4, ban);

        ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta playermeta = playerhead.getItemMeta();
        playermeta.setDisplayName(whotounban.getDisplayName());
        playerhead.setItemMeta(playermeta);
        unbanmenu.setItem(14, playerhead);

        ItemStack cancel = new ItemStack(Material.BARRIER, 1);
        ItemMeta cancelmeta = cancel.getItemMeta();
        cancelmeta.setDisplayName(ChatColor.RED + "Cancel Unban");
        cancel.setItemMeta(cancelmeta);
        unbanmenu.setItem(16, cancel);

        player.openInventory(unbanmenu);
    }
}
