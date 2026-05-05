package com.example.addon.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.IGetToBlockProcess;
import baritone.api.process.PathingCommand;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.BlockOptionalMeta;
import com.example.addon.modules.ChestSorter;
import com.example.addon.utils.Utils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.render.TunnelESP;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;


public class GetToBlockPathing implements IGetToBlockProcess{
    private MinecraftClient mc = MinecraftClient.getInstance();
    private World world = MeteorClient.mc.world;
    private static final BlockPos.Mutable BP = new BlockPos.Mutable();
    IBaritone baritone;

    @Override
    public void getToBlock(BlockOptionalMeta block) {

    }

    public GetToBlockPathing(IBaritone baritone){
        this.baritone = baritone;
    }

    public void getToBlockPos(BetterBlockPos blockPos) {
        BetterBlockPos i = searchWalkPath(blockPos);
        System.out.println("searchWalkPath block position: " + i);
        if(i!=null) baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(i));
        else{
            System.out.println("No searchWalkPaths found, trying to go straight to the chest");
            baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(blockPos));
        }
    }

    public void resetMovement(){
        getToBlockPos(new BetterBlockPos(mc.player.getBlockPos().add(Utils.random(-10, 10), Utils.random(-5,5), Utils.random(-10, 10))));
    }

    @Override
    public boolean blacklistClosest() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public void onLostControl() {

    }

    @Override
    public String displayName0() {
        return "";
    }

    private BetterBlockPos searchWalkPath(BetterBlockPos blockPos){
        Context ctx = new Context();
        final int radius = 7;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for (int i = x - radius; i <= radius+x; i++) {
            for (int j = y - radius; j <= radius + y; j++) {
                for (int k = z - radius; k < radius + z; k++) {
                    if (isTunnel(ctx, x, y, z)) return new BetterBlockPos(i,j,k);
                }
            }
        }
        return null;
    }

    private boolean isTunnel(Context ctx, int x, int y, int z) {
        if (!canWalkIn(ctx, x, y, z)) return false;

        TunnelSide s1 = getTunnelSide(ctx, x + 1, y, z);
        if (s1 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s2 = getTunnelSide(ctx, x - 1, y, z);
        if (s2 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s3 = getTunnelSide(ctx, x, y, z + 1);
        if (s3 == TunnelSide.PartiallyBlocked) return false;

        TunnelSide s4 = getTunnelSide(ctx, x, y, z - 1);
        if (s4 == TunnelSide.PartiallyBlocked) return false;

        return (s1 == TunnelSide.Walkable && s2 == TunnelSide.Walkable && s3 == TunnelSide.FullyBlocked && s4 == TunnelSide.FullyBlocked) || (s1 == TunnelSide.FullyBlocked && s2 == TunnelSide.FullyBlocked && s3 == TunnelSide.Walkable && s4 == TunnelSide.Walkable);
    }

    private TunnelSide getTunnelSide(Context ctx, int x, int y, int z) {
        if (canWalkIn(ctx, x, y, z)) return TunnelSide.Walkable;
        if (!canWalkThrough(ctx, x, y, z) && !canWalkThrough(ctx, x, y + 1, z)) return TunnelSide.FullyBlocked;
        return TunnelSide.PartiallyBlocked;
    }

    private boolean canWalkOn(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);

        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;

        return !state.getCollisionShape(mc.world, BP.set(x, y, z)).isEmpty();
    }

    private boolean canWalkThrough(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);

        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty()) return false;

        return state.getCollisionShape(mc.world, BP.set(x, y, z)).isEmpty();
    }

    private boolean canWalkIn(Context ctx, int x, int y, int z) {
        if (!canWalkOn(ctx, x, y - 1, z)) return false;
        if (!canWalkThrough(ctx, x, y, z)) return false;
        if (canWalkThrough(ctx, x, y + 2, z)) return false;
        return canWalkThrough(ctx, x, y + 1, z);
    }


    private enum TunnelSide {
        Walkable,
        PartiallyBlocked,
        FullyBlocked
    }

    private static class Context {
        private final World world;

        private Chunk lastChunk;

        public Context() {
            this.world = MeteorClient.mc.world;
        }

        public BlockState get(int x, int y, int z) {
            if (world.isOutOfHeightLimit(y)) return Blocks.VOID_AIR.getDefaultState();

            int cx = x >> 4;
            int cz = z >> 4;

            Chunk chunk;

            if (lastChunk != null && lastChunk.getPos().x == cx && lastChunk.getPos().z == cz) chunk = lastChunk;
            else chunk = world.getChunk(cx, cz, ChunkStatus.FULL, false);

            if (chunk == null) return Blocks.VOID_AIR.getDefaultState();

            ChunkSection section = chunk.getSectionArray()[chunk.getSectionIndex(y)];
            if (section == null) return Blocks.VOID_AIR.getDefaultState();

            lastChunk = chunk;
            return section.getBlockState(x & 15, y & 15, z & 15);
        }
    }
}
