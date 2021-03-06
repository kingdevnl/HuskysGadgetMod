package net.thegaminghuskymc.gadgetmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.thegaminghuskymc.gadgetmod.HuskyGadgetMod;
import net.thegaminghuskymc.gadgetmod.Reference;
import net.thegaminghuskymc.gadgetmod.object.Bounds;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityServer;
import net.thegaminghuskymc.gadgetmod.util.CollisionHelper;
import net.thegaminghuskymc.gadgetmod.util.Colorable;

import javax.annotation.Nullable;
import java.util.List;

public class BlockServer extends BlockDevice implements ITileEntityProvider {

    private static final Bounds BODY_BOUNDS = new Bounds(5 * 0.0625, 0.0, 1 * 0.0625, 14 * 0.0625, 5 * 0.0625, 15 * 0.0625);
    private static final AxisAlignedBB BODY_BOX_NORTH = CollisionHelper.getBlockBounds(EnumFacing.NORTH, BODY_BOUNDS);
    private static final AxisAlignedBB BODY_BOX_EAST = CollisionHelper.getBlockBounds(EnumFacing.EAST, BODY_BOUNDS);
    private static final AxisAlignedBB BODY_BOX_SOUTH = CollisionHelper.getBlockBounds(EnumFacing.SOUTH, BODY_BOUNDS);
    private static final AxisAlignedBB BODY_BOX_WEST = CollisionHelper.getBlockBounds(EnumFacing.WEST, BODY_BOUNDS);
    private static final AxisAlignedBB[] BODY_BOUNDING_BOX = { BODY_BOX_SOUTH, BODY_BOX_WEST, BODY_BOX_NORTH, BODY_BOX_EAST };

    private static final Bounds TRAY_BOUNDS = new Bounds(0.5 * 0.0625, 0.0, 3.5 * 0.0625, 5 * 0.0625, 1 * 0.0625, 12.5 * 0.0625);
    private static final AxisAlignedBB TRAY_BOX_NORTH = CollisionHelper.getBlockBounds(EnumFacing.NORTH, TRAY_BOUNDS);
    private static final AxisAlignedBB TRAY_BOX_EAST = CollisionHelper.getBlockBounds(EnumFacing.EAST, TRAY_BOUNDS);
    private static final AxisAlignedBB TRAY_BOX_SOUTH = CollisionHelper.getBlockBounds(EnumFacing.SOUTH, TRAY_BOUNDS);
    private static final AxisAlignedBB TRAY_BOX_WEST = CollisionHelper.getBlockBounds(EnumFacing.WEST, TRAY_BOUNDS);
    private static final AxisAlignedBB[] TRAY_BOUNDING_BOX = { TRAY_BOX_SOUTH, TRAY_BOX_WEST, TRAY_BOX_NORTH, TRAY_BOX_EAST };

    private static final Bounds PAPER_BOUNDS = new Bounds(13 * 0.0625, 0.0, 4 * 0.0625, 1.0, 9 * 0.0625, 12 * 0.0625);
    private static final AxisAlignedBB PAPER_BOX_NORTH = CollisionHelper.getBlockBounds(EnumFacing.NORTH, PAPER_BOUNDS);
    private static final AxisAlignedBB PAPER_BOX_EAST = CollisionHelper.getBlockBounds(EnumFacing.EAST, PAPER_BOUNDS);
    private static final AxisAlignedBB PAPER_BOX_SOUTH = CollisionHelper.getBlockBounds(EnumFacing.SOUTH, PAPER_BOUNDS);
    private static final AxisAlignedBB PAPER_BOX_WEST = CollisionHelper.getBlockBounds(EnumFacing.WEST, PAPER_BOUNDS);
    private static final AxisAlignedBB[] PAPER_BOUNDING_BOX = { PAPER_BOX_SOUTH, PAPER_BOX_WEST, PAPER_BOX_NORTH, PAPER_BOX_EAST };

    private static final AxisAlignedBB SELECTION_BOUNDING_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    public BlockServer() {
        super(Material.ANVIL);
        this.setCreativeTab(HuskyGadgetMod.deviceDecoration);
        this.setUnlocalizedName("server");
        this.setRegistryName(Reference.MOD_ID, "server");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityServer();
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
        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return SELECTION_BOUNDING_BOX;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_)
    {
        EnumFacing facing = state.getValue(FACING);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, BODY_BOUNDING_BOX[facing.getHorizontalIndex()]);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, TRAY_BOUNDING_BOX[facing.getHorizontalIndex()]);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, PAPER_BOUNDING_BOX[facing.getHorizontalIndex()]);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return state.withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, BlockColored.COLOR);
    }
}