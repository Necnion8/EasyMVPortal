package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPManager;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPortal;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PortalCreator {

    private static final Material TOOL_MATERIAL;

    static {
        Material m = Material.getMaterial("GOLDEN_HOE");
        TOOL_MATERIAL = m != null ? m : Material.getMaterial("GOLD_HOE");  // legacy
    }

    private final EasyMVPortal instance;
    private final Player creator;
    private final EMVPManager mvp;
    //
    private ItemStack tool;
    private Location portalA1, portalA2, portalB1, portalB2;
    private Portal currentPortal;
    //
    private AnvilGUI anvilGUI;
    private boolean guiRetry;
    //
    private long rightFixLast;


    public PortalCreator(EasyMVPortal instance, Player creator) {
        this.instance = instance;
        this.creator = creator;
        this.mvp = instance.getMVP();
    }

    static public PortalCreator getCreator(Player player, boolean create) {
        return EasyMVPortal.getInstance().getCreatorSession(player, create);
    }

    public void initAndReplaceMainHandItem() {
        if (tool != null) {
            selectToolItem();
            return;  // instanced
        }

        tool = new ItemStack(Material.AIR);
        currentPortal = Portal.PORTAL_A;
        Lang.SELECT_PORTAL1.sendTo(creator);
        replaceMainHandItem();
        Sound.EXPERIENCE_ORB_PICKUP.playTo(creator, 1f, 2f);

    }

    public void close() {
        anvilGUI = null;
        ItemStack[] contents = creator.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (tool.equals(contents[i])) {
                contents[i] = null;
            }
        }
        creator.getInventory().setContents(contents);
        creator.updateInventory();

        instance.removeCreator(creator);
    }

    public void removeToolItems() {
        ItemStack[] contents = creator.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (tool.equals(contents[i])) {
                contents[i] = null;
            }
        }
        creator.getInventory().setContents(contents);
    }

    private void replaceMainHandItem() {
        removeToolItems();
        ItemMeta meta;

        if (Portal.PORTAL_A.equals(currentPortal)) {
            tool.setType(TOOL_MATERIAL);
            meta = tool.getItemMeta();
            meta.setDisplayName(Lang.TOOL_PORTAL1_WAND_DISPLAY.format());
            meta.setLore(Arrays.asList(Lang.TOOL_PORTAL1_WAND_LORE.formatMultiline()));
            tool.setItemMeta(meta);

        } else if (Portal.PORTAL_B.equals(currentPortal)) {
            tool.setType(Material.DIAMOND_HOE);
            meta = tool.getItemMeta();
            meta.setDisplayName(Lang.TOOL_PORTAL2_WAND_DISPLAY.format());
            meta.setLore(Arrays.asList(Lang.TOOL_PORTAL2_WAND_LORE.formatMultiline()));
            tool.setItemMeta(meta);

        } else {
            tool.setType(Material.ANVIL);
            meta = tool.getItemMeta();
            meta.setDisplayName(Lang.TOOL_NAME_ANVIL_DISPLAY.format());
            meta.setLore(Collections.emptyList());
            tool.setItemMeta(meta);
        }

        creator.getInventory().setItemInHand(tool);
    }

    private boolean createPortals(String p1Name, String p2Name) {
        return mvp.createPortals(
                creator.getName(),
                p1Name, portalA1.getWorld(), portalA1, portalA2,
                p2Name, portalB1.getWorld(), portalB1, portalB2
        );
    }

    public void selectToolItem() {
        ItemStack[] contents = creator.getInventory().getContents();
        for (int i = 0; i <= 8; i++) {
            if (tool.equals(contents[i])) {
                creator.getInventory().setHeldItemSlot(i);
                return;
            }
        }
    }


    // events
    public void onEvent(PlayerInteractEvent event) {
        if (!tool.equals(creator.getInventory().getItemInHand()) || !creator.equals(event.getPlayer())) return;
        event.setCancelled(true);
        Location bLoc = (event.getClickedBlock() != null) ? event.getClickedBlock().getLocation() : null;

        if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            if (!mvp.isMVWorld(bLoc.getWorld())) {
                Lang.NOT_LOADED_MULTIVERSE.sendTo(creator);
                return;
            }

            if (Portal.PORTAL_A.equals(currentPortal)) {
                portalA1 = bLoc;
                Lang.SET_PORTAL1_FIRST_POS.sendTo(creator, bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ());

            } else if (Portal.PORTAL_B.equals(currentPortal)) {
                portalB1 = bLoc;
                Lang.SET_PORTAL2_FIRST_POS.sendTo(creator, bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ());
            }

        } else if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction()) && !creator.isSneaking()) {
            if (!fixRightClick())
                return;

            if (!mvp.isMVWorld(bLoc.getWorld())) {
                Lang.NOT_LOADED_MULTIVERSE.sendTo(creator);
                return;
            }

            if (Portal.PORTAL_A.equals(currentPortal)) {
                portalA2 = bLoc;
                Lang.SET_PORTAL1_SECOND_POS.sendTo(creator, bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ());

            } else if (Portal.PORTAL_B.equals(currentPortal)) {
                portalB2 = bLoc;
                Lang.SET_PORTAL2_SECOND_POS.sendTo(creator, bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ());
            }

        } else if ((Action.RIGHT_CLICK_BLOCK.equals(event.getAction()) || Action.RIGHT_CLICK_AIR.equals(event.getAction())) && creator.isSneaking()) {
            if (Portal.PORTAL_A.equals(currentPortal)) {
                if (portalA1 != null && portalA2 != null) {
                    currentPortal = Portal.PORTAL_B;
                    portalA1.setYaw(creator.getLocation().getYaw());

                    Lang.SELECT_PORTAL2.sendTo(creator);
                    replaceMainHandItem();
                    Sound.NOTE_BASS.playTo(creator, 1f, 2f);

                } else {
                    // not selected
                    Sound.NOTE_BASS.playTo(creator, 1f, 1f);
                }

            } else if (Portal.PORTAL_B.equals(currentPortal)) {
                if (portalB1 != null && portalB2 != null) {
                    currentPortal = null;
                    portalB1.setYaw(creator.getLocation().getYaw());

                    if (instance.isUseGUI()) {
                        openAnvilGui(Lang.GUI_INSERT_PORTAL_NAME.format());
                    } else {
                        Lang.CREATE_AND_INSERT_PORTAL_NAME.sendTo(creator);
                    }
                    replaceMainHandItem();
                    Sound.EXPERIENCE_ORB_PICKUP.playTo(creator, 1f, 2f);

                } else {
                    // not selected
                    Sound.NOTE_BASS.playTo(creator, 1f, 1f);
                }
            }
        }

    }

    public void onEvent(PlayerDropItemEvent event) {
        if (!tool.equals(event.getItemDrop().getItemStack()) || !creator.equals(event.getPlayer())) return;
        event.getItemDrop().remove();

        Lang.CANCEL.sendTo(creator);
        close();
        Sound.BLAZE_HURT.playTo(creator, 1f, 0f);
    }

    public void onEvent(InventoryClickEvent event) {
        if (!tool.equals(event.getCurrentItem()) || !creator.equals(event.getWhoClicked())) return;
        event.setResult(Event.Result.DENY);
        creator.updateInventory();
    }

    public void onEvent(PlayerDeathEvent event) {
        event.getDrops().remove(tool);
        Lang.CANCEL.sendTo(creator);
        close();
    }

    public void onEvent(PlayerSwapHandItemsEvent event) {
        if (!tool.equals(event.getOffHandItem()) || !creator.equals(event.getPlayer())) return;
        event.setCancelled(true);
    }

    public void onCommand(String[] args) {
        if (args.length < 1) {
            Lang.CREATE_AND_INSERT_PORTAL_NAME.sendTo(creator);
            return;
        }

        if (Portal.PORTAL_A.equals(currentPortal)) {
            Lang.SELECT_PORTAL1.sendTo(creator);
            return;
//        } else if (Portal.PORTAL_B.equals(currentPortal)) {
        } else if (portalB1 == null || portalB2 == null) {
            Lang.SELECT_PORTAL2.sendTo(creator);
            return;
        }

        String name = String.join("_", args).replace("　", "_");
        String p1Name = instance.getPluginConfig().getPortal1Name(name);
        String p2Name = instance.getPluginConfig().getPortal2Name(name);

        boolean exists = mvp.streamPortals()
                .map(EMVPortal::getName)
                .anyMatch(p -> p1Name.equalsIgnoreCase(p) || p2Name.equalsIgnoreCase(p));

        if (exists) {
            // retry
            Lang.ALREADY_EXISTS_PORTAL_NAME.sendTo(creator);
            Lang.CREATE_AND_INSERT_PORTAL_NAME.sendTo(creator);
            Sound.NOTE_BASS.playTo(creator, 1f, 1f);
        } else {
            // create
            close();
            if (createPortals(p1Name, p2Name)) {
                Lang.PORTAL_CREATED.sendTo(creator, name);
                Sound.PLAYER_LEVELUP.playTo(creator, 1f, 1f);
            } else {
                Lang.PORTAL_CREATE_FAILED.sendTo(creator);
                Sound.NOTE_BASS.playTo(creator, 1f, 1f);
            }
        }
    }


    //
    private boolean fixRightClick() {
        // 前のクリックから10ms以上待つ (PlayerInteractEventが二重に発火するFix)
        if (System.currentTimeMillis() - rightFixLast > 10) {
            rightFixLast = System.currentTimeMillis();
            return true;
        }
        return false;
    }


    // anvil gui
    private void openAnvilGui(String title) {
        if (anvilGUI != null) {
            anvilGUI.closeInventory();
        }
        guiRetry = false;
        anvilGUI = new AnvilGUI.Builder()
                .title(title)
                .text("name")
                .plugin(instance)
                .onClose(this::onAnvilClose)
                .onClick(this::onAnvilClick)
                .open(creator);
    }

    private void onAnvilClose(AnvilGUI.StateSnapshot stateSnapshot) {
        if (anvilGUI != null && !guiRetry) {
            Lang.CANCEL.sendTo(creator);
            close();
            Sound.NOTE_BASS.playTo(creator, 1f, 0f);
        }
    }

    private List<AnvilGUI.ResponseAction> onAnvilClick(int slot, AnvilGUI.StateSnapshot stateSnapshot) {
        String name = stateSnapshot.getText().replaceAll(" ", "_").replaceAll("　", "_");
        String p1Name = instance.getPluginConfig().getPortal1Name(name);
        String p2Name = instance.getPluginConfig().getPortal2Name(name);

        boolean exists = mvp.streamPortals()
                .map(EMVPortal::getName)
                .anyMatch(p -> p1Name.equalsIgnoreCase(p) || p2Name.equalsIgnoreCase(p));

        if (exists) {
            guiRetry = true;
            openAnvilGui(Lang.GUI_INSERT_PORTAL_NAME_ALREADY_EXISTS.format());
            return Collections.singletonList(AnvilGUI.ResponseAction.close());
        }
        guiRetry = false;

        // portal creation
        close();
        if (createPortals(p1Name, p2Name)) {
            Lang.PORTAL_CREATED.sendTo(creator, name);
            Sound.PLAYER_LEVELUP.playTo(creator, 1f, 1f);
        } else {
            Lang.PORTAL_CREATE_FAILED.sendTo(creator);
            Sound.NOTE_BASS.playTo(creator, 1f, 1f);
        }
        return Collections.singletonList(AnvilGUI.ResponseAction.close());
    }


    private enum Portal {
        PORTAL_A, PORTAL_B
    }

}
