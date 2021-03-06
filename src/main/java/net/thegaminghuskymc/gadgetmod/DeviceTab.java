package net.thegaminghuskymc.gadgetmod;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.thegaminghuskymc.gadgetmod.init.GadgetBlocks;

import java.util.Random;

public class DeviceTab extends CreativeTabs {

    private ItemStack icon = ItemStack.EMPTY;
    private boolean hasSearchBar = false;
    private boolean displayRandom = true;
    protected final Random RANDOM = new Random();
    private int tempIndex = 0;
    private ItemStack tempDisplayStack = ItemStack.EMPTY;

    public DeviceTab(String label) {
        super(label);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemStack getIconItemStack() {
        if(this.displayRandom) {
            if (Minecraft.getSystemTime() % 120 == 0) {
                this.updateDisplayStack();
            }
            return this.tempDisplayStack;
        }
        else return this.icon;
    }

    @SideOnly(Side.CLIENT)
    private void updateDisplayStack() {
        if (this.displayRandom) {
            NonNullList<ItemStack> itemStacks = NonNullList.create();
            this.displayAllRelevantItems(itemStacks);
            this.tempDisplayStack = !itemStacks.isEmpty() ? itemStacks.get(tempIndex) : ItemStack.EMPTY;
            if (++tempIndex >= itemStacks.size()) tempIndex = 0;
        } else {
            if(this.icon.isEmpty()) {
                this.tempDisplayStack = new ItemStack(Items.DIAMOND);
            }
            this.tempDisplayStack = this.icon;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemStack getTabIconItem() {
        return this.getIconItemStack();
    }

}
