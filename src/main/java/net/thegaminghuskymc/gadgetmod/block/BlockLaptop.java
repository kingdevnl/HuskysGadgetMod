package net.thegaminghuskymc.gadgetmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.thegaminghuskymc.gadgetmod.HuskyGadgetMod;
import net.thegaminghuskymc.gadgetmod.Reference;
import net.thegaminghuskymc.gadgetmod.core.Laptop;
import net.thegaminghuskymc.gadgetmod.init.GadgetItems;
import net.thegaminghuskymc.gadgetmod.object.Bounds;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityLaptop;
import net.thegaminghuskymc.gadgetmod.util.Colorable;
import net.thegaminghuskymc.gadgetmod.util.TileEntityUtil;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockLaptop extends BlockDevice implements ITileEntityProvider {

    public static final PropertyEnum TYPE = PropertyEnum.create("type", Type.class);

    private static final AxisAlignedBB[] SCREEN_BOXES = new Bounds(13 * 0.0625, 0.0625, 1 * 0.0625, 1.0, 12 * 0.0625, 0.9375).getRotatedBounds();
    private static final AxisAlignedBB BODY_OPEN_BOX = new AxisAlignedBB(1 * 0.0625, 0.0, 1 * 0.0625, 13 * 0.0625, 1 * 0.0625, 15 * 0.0625);
    private static final AxisAlignedBB BODY_CLOSED_BOX = new AxisAlignedBB(1 * 0.0625, 0.0, 1 * 0.0625, 13 * 0.0625, 2 * 0.0625, 15 * 0.0625);
    private static final AxisAlignedBB SELECTION_BOX_OPEN = new AxisAlignedBB(0, 0, 0, 1, 12 * 0.0625, 1);
    private static final AxisAlignedBB SELECTION_BOX_CLOSED = new AxisAlignedBB(0, 0, 0, 1, 3 * 0.0625, 1);

    public BlockLaptop()
    {
        super(Material.ANVIL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, Type.BASE));
        this.setCreativeTab(HuskyGadgetMod.deviceBlocks);
        this.setUnlocalizedName("laptop");
        this.setRegistryName(Reference.MOD_ID, "laptop");
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        TileEntity tileEntity = source.getTileEntity(pos);
        if(tileEntity instanceof TileEntityLaptop)
        {
            TileEntityLaptop laptop = (TileEntityLaptop) tileEntity;
            if(laptop.isOpen())
            {
                return SELECTION_BOX_OPEN;
            }
            else
            {
                return SELECTION_BOX_CLOSED;
            }
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityLaptop)
        {
            TileEntityLaptop laptop = (TileEntityLaptop) tileEntity;
            if(laptop.isOpen())
            {
                Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, BODY_OPEN_BOX);
                Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, SCREEN_BOXES[state.getValue(FACING).getHorizontalIndex()]);
            }
            else
            {
                Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, BODY_CLOSED_BOX);
            }
            return;
        }
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityLaptop)
        {
            TileEntityLaptop laptop = (TileEntityLaptop) tileEntity;

            if(playerIn.isSneaking())
            {
                if(!worldIn.isRemote)
                {
                    laptop.openClose();
                }
            }
            else
            {
                if(side == state.getValue(FACING).rotateYCCW())
                {
                    ItemStack heldItem = playerIn.getHeldItem(hand);
                    if(!heldItem.isEmpty() && heldItem.getItem() == GadgetItems.flash_drive)
                    {
                        if(!worldIn.isRemote)
                        {
                            if(laptop.getFileSystem().setAttachedDrive(heldItem.copy()))
                            {
                                heldItem.shrink(1);
                                TileEntityUtil.markBlockForUpdate(worldIn, pos);
                            }
                            else
                            {
                                playerIn.sendMessage(new TextComponentString("No more available USB slots!"));
                            }
                        }
                        return true;
                    }

                    if(!worldIn.isRemote)
                    {
                        ItemStack stack = laptop.getFileSystem().removeAttachedDrive();
                        if(stack != null)
                        {
                            BlockPos summonPos = pos.offset(state.getValue(FACING).rotateYCCW());
                            worldIn.spawnEntity(new EntityItem(worldIn, summonPos.getX() + 0.5, summonPos.getY(), summonPos.getZ() + 0.5, stack));
                            TileEntityUtil.markBlockForUpdate(worldIn, pos);
                        }
                    }
                    return true;
                }

                if(laptop.isOpen() && worldIn.isRemote)
                {
                    playerIn.openGui(HuskyGadgetMod.instance, Laptop.ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityLaptop)
        {
            TileEntityLaptop laptop = (TileEntityLaptop) tileEntity;

            NBTTagCompound tileEntityTag = new NBTTagCompound();
            laptop.writeToNBT(tileEntityTag);
            tileEntityTag.removeTag("x");
            tileEntityTag.removeTag("y");
            tileEntityTag.removeTag("z");
            tileEntityTag.removeTag("id");
            tileEntityTag.removeTag("open");

            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("BlockEntityTag", tileEntityTag);

            ItemStack drop = new ItemStack(Item.getItemFromBlock(this));
            drop.setTagCompound(compound);

            worldIn.spawnEntity(new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof Colorable)
        {
            Colorable colorable = (Colorable) tileEntity;
            state = state.withProperty(BlockColored.COLOR, colorable.getColor());
        }
        return state.withProperty(TYPE, Type.BASE);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return state.withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityLaptop();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(FACING)).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(TYPE, Type.BASE);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
        builder.add(FACING, TYPE);
        builder.add(BlockColored.COLOR);
        return builder.build();
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    public enum Type implements IStringSerializable
    {
        BASE, SCREEN;

        @Override
        public String getName()
        {
            return name().toLowerCase();
        }

    }
}