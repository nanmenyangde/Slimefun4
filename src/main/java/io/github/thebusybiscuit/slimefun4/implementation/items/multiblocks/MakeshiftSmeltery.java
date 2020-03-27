package io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks;

import io.github.thebusybiscuit.cscorelib2.inventory.InvUtils;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MakeshiftSmeltery extends MultiBlockMachine {

    public MakeshiftSmeltery() {
        super(Categories.MACHINES_1, SlimefunItems.MAKESHIFT_SMELTERY, new ItemStack[]{
                null, new ItemStack(Material.OAK_FENCE), null,
                new ItemStack(Material.BRICKS), new CustomItem(Material.DISPENSER, "Dispenser (Facing up)"), new ItemStack(Material.BRICKS),
                null, new ItemStack(Material.FLINT_AND_STEEL), null
        }, new ItemStack[0], BlockFace.DOWN);
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < recipes.size() - 1; i += 2) {
            items.add(recipes.get(i)[0]);
            items.add(recipes.get(i + 1)[0]);
        }

        return items;
    }

    @Override
    public void onInteract(Player p, Block b) {
        Block dispBlock = b.getRelative(BlockFace.DOWN);
        Dispenser disp = (Dispenser) dispBlock.getState();
        Inventory inv = disp.getInventory();
        List<ItemStack[]> inputs = RecipeType.getRecipeInputList(this);

        for (int i = 0; i < inputs.size(); i++) {
            if (canCraft(inv, inputs, i)) {
                ItemStack output = RecipeType.getRecipeOutputList(this, inputs.get(i)).clone();

                if (Slimefun.hasUnlocked(p, output, true)) {
                    Inventory outputInv = findOutputInventory(output, dispBlock, inv);

                    if (outputInv != null) {
                        craft(p, b, inv, inputs.get(i), output, outputInv);
                    } else SlimefunPlugin.getLocal().sendMessage(p, "machines.full-inventory", true);
                }

                return;
            }
        }

        SlimefunPlugin.getLocal().sendMessage(p, "machines.unknown-material", true);
    }

    private void craft(Player p, Block b, Inventory inv, ItemStack[] recipe, ItemStack output, Inventory outputInv) {
        for (ItemStack removing : recipe) {
            if (removing != null) {
                InvUtils.removeItem(inv, removing.getAmount(), true, stack -> SlimefunUtils.isItemSimilar(stack, removing, true));
            }
        }

        outputInv.addItem(output);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
        p.getWorld().playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);

        Block fire = b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        fire.getWorld().playEffect(fire.getLocation(), Effect.STEP_SOUND, fire.getType());
        fire.setType(Material.AIR);
    }

    private boolean canCraft(Inventory inv, List<ItemStack[]> inputs, int i) {
        for (ItemStack converting : inputs.get(i)) {
            if (converting != null) {
                for (int j = 0; j < inv.getContents().length; j++) {
                    if (j == (inv.getContents().length - 1) && !SlimefunUtils.isItemSimilar(converting, inv.getContents()[j], true)) {
                        return false;
                    } else if (SlimefunUtils.isItemSimilar(inv.getContents()[j], converting, true)) break;
                }
            }
        }

        return true;
    }

}