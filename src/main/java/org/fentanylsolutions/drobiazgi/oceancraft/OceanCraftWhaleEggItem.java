package org.fentanylsolutions.drobiazgi.oceancraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.Config;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OceanCraftWhaleEggItem extends Item {

    private final OceanCraftWhaleManager.WhaleVariant whaleVariant;
    private final int primaryColor;
    private final int secondaryColor;
    @SideOnly(Side.CLIENT)
    private IIcon overlayIcon;

    public OceanCraftWhaleEggItem(OceanCraftWhaleManager.WhaleVariant whaleVariant, int primaryColor,
        int secondaryColor, String unlocalizedName) {
        this.whaleVariant = whaleVariant;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        setUnlocalizedName(unlocalizedName);
        setHasSubtypes(false);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        Block block = world.getBlock(x, y, z);
        x += Facing.offsetsXForSide[side];
        y += Facing.offsetsYForSide[side];
        z += Facing.offsetsZForSide[side];
        double spawnYOffset = side == 1 && block.getRenderType() == 11 ? 0.5D : 0.0D;
        Entity entity = spawnWhale(world, x + 0.5D, y + spawnYOffset, z + 0.5D);

        if (entity != null) {
            applyCustomName(itemStack, entity);
            if (!player.capabilities.isCreativeMode) {
                --itemStack.stackSize;
            }
        }

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return itemStack;
        }

        MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, true);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return itemStack;
        }

        int x = hit.blockX;
        int y = hit.blockY;
        int z = hit.blockZ;

        if (!world.canMineBlock(player, x, y, z)) {
            return itemStack;
        }
        if (!player.canPlayerEdit(x, y, z, hit.sideHit, itemStack)) {
            return itemStack;
        }
        if (!(world.getBlock(x, y, z) instanceof BlockLiquid)) {
            return itemStack;
        }

        Entity entity = spawnWhale(world, x, y, z);
        if (entity != null) {
            applyCustomName(itemStack, entity);
            if (!player.capabilities.isCreativeMode) {
                --itemStack.stackSize;
            }
        }

        return itemStack;
    }

    private Entity spawnWhale(World world, double x, double y, double z) {
        EntityLiving whale = OceanCraftWhaleManager.spawnWhaleFromEgg(world, whaleVariant);
        if (whale == null) {
            return null;
        }

        whale.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F), 0.0F);
        whale.rotationYawHead = whale.rotationYaw;
        whale.renderYawOffset = whale.rotationYaw;
        whale.onSpawnWithEgg(null);

        if (!world.spawnEntityInWorld(whale)) {
            return null;
        }

        whale.playLivingSound();
        return whale;
    }

    private void applyCustomName(ItemStack itemStack, Entity entity) {
        if (entity instanceof EntityLiving && itemStack.hasDisplayName()) {
            ((EntityLiving) entity).setCustomNameTag(itemStack.getDisplayName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int renderPass) {
        return renderPass == 0 ? primaryColor : secondaryColor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int metadata, int renderPass) {
        return renderPass > 0 ? overlayIcon : itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs creativeTab, List itemStacks) {
        if (!Config.isOceanCraftWhaleSplitEnabled()) {
            return;
        }

        itemStacks.add(new ItemStack(item));
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        if (!Config.isOceanCraftWhaleSplitEnabled()) {
            return new CreativeTabs[0];
        }

        return new CreativeTabs[] { getCreativeTab(), CreativeTabs.tabAllSearch };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("minecraft:spawn_egg");
        overlayIcon = register.registerIcon("minecraft:spawn_egg_overlay");
    }
}
