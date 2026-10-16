package io.github.httpsluch.timecapsule.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.httpsluch.timecapsule.TimeCapsule;
import io.github.httpsluch.timecapsule.block.CapsuleBlock;
import io.github.httpsluch.timecapsule.block.CapsuleBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

//? if >=26.1 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.phys.Vec3;
//?} else {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
*///?}

/**
 * Draws a placed capsule's lid so it swings open like a vanilla chest's: an eased half-second swing up
 * to 90 degrees. The body (and padlock) is the plain block model; only these six faces are drawn each
 * frame. The lid uses the block textures through the block atlas, so it matches the item model face for
 * face and a sealed lid's clock keeps its animation. The angle comes from
 * {@link CapsuleBlockEntity#lidOpenness(float)}, so nothing ticks.
 */
public class CapsuleLidRenderer implements BlockEntityRenderer<CapsuleBlockEntity/*? if >=26.1 {*/, CapsuleLidRenderer.State/*?}*/> {
    // The lid of the facing-north block model in pixels: x 1-15, y 10-14, z 1-15. Each row is the
    // outward normal, the corners top-left, bottom-left, bottom-right and top-right as seen from outside
    // (the block model's vertex order), then u1 v1 u2 v2 as in models/item/time_capsule.json.
    private static final float[][] FACES = {
        {0, 0, -1, 15, 14, 1, 15, 10, 1, 1, 10, 1, 1, 14, 1, 1, 2, 15, 6},     // north, the front
        {0, 0, 1, 1, 14, 15, 1, 10, 15, 15, 10, 15, 15, 14, 15, 1, 2, 15, 6},  // south
        {1, 0, 0, 15, 14, 15, 15, 10, 15, 15, 10, 1, 15, 14, 1, 1, 2, 15, 6},  // east
        {-1, 0, 0, 1, 14, 1, 1, 10, 1, 1, 10, 15, 1, 14, 15, 1, 2, 15, 6},     // west
        {0, 1, 0, 1, 14, 1, 1, 14, 15, 15, 14, 15, 15, 14, 1, 1, 1, 15, 15},   // up
        {0, -1, 0, 1, 10, 15, 1, 10, 1, 15, 10, 1, 15, 10, 15, 1, 1, 15, 15},  // down, the inside
    };
    private static final String[] TEXTURES = {"block/time_capsule_front", "block/time_capsule_side",
        "block/time_capsule_top", "block/time_capsule_inside", "block/time_capsule_top_sealed"};
    /** Index into {@link #TEXTURES} for each face; the top switches to {@link #SEALED_TOP} once sealed. */
    private static final int[] FACE_TEXTURE = {0, 1, 1, 1, 2, 3};
    private static final int TOP_FACE = 4;
    private static final int SEALED_TOP = 4;

    //? if >=26.1 {
    private static final SpriteId[] SPRITES = new SpriteId[TEXTURES.length];

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            SPRITES[i] = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TimeCapsule.id(TEXTURES[i]));
        }
    }

    private final SpriteGetter sprites;

    public CapsuleLidRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    /** What the lid needs from the block entity, captured once per frame. */
    public static class State extends BlockEntityRenderState {
        boolean visible;
        boolean sealed;
        float facingAngle;
        float lidAngle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CapsuleBlockEntity capsule, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(capsule, state, partialTick, cameraPos, breakProgress);
        BlockState block = capsule.getBlockState();
        state.visible = block.hasProperty(CapsuleBlock.SEALED);
        if (state.visible) {
            state.sealed = block.getValue(CapsuleBlock.SEALED);
            state.facingAngle = facingAngle(block);
            state.lidAngle = capsule.lidOpenness(partialTick) * 90.0F;
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) {
            return;
        }
        TextureAtlasSprite[] sprite = new TextureAtlasSprite[SPRITES.length];
        for (int i = 0; i < SPRITES.length; i++) {
            sprite[i] = sprites.get(SPRITES[i]);
        }
        int light = state.lightCoords;
        boolean sealed = state.sealed;
        pose.pushPose();
        place(pose, state.facingAngle, state.lidAngle);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS),
            (last, out) -> drawLid(last, out, sprite, sealed, light));
        pose.popPose();
    }
    //?} else {
    /*public CapsuleLidRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CapsuleBlockEntity capsule, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState block = capsule.getBlockState();
        if (!block.hasProperty(CapsuleBlock.SEALED)) {
            return;
        }
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite[] sprite = new TextureAtlasSprite[TEXTURES.length];
        for (int i = 0; i < TEXTURES.length; i++) {
            sprite[i] = atlas.apply(TimeCapsule.id(TEXTURES[i]));
        }
        pose.pushPose();
        place(pose, facingAngle(block), capsule.lidOpenness(partialTick) * 90.0F);
        drawLid(pose.last(), buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)), sprite,
            block.getValue(CapsuleBlock.SEALED), light);
        pose.popPose();
    }
    *///?}

    @Override
    public int getViewDistance() {
        // The body is a block model and stays visible from far away, so keep its lid on longer than a chest's 64 blocks.
        return 128;
    }

    /** The blockstate's y rotation (clockwise from north) as a turn about +Y. */
    private static float facingAngle(BlockState block) {
        return 180.0F - block.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
    }

    private static void place(PoseStack pose, float facingAngle, float lidAngle) {
        pose.rotateAround(Axis.YP.rotationDegrees(facingAngle), 0.5F, 0.0F, 0.5F);
        // Hinge along the lid's back bottom edge; a positive x rotation lifts the front.
        pose.rotateAround(Axis.XP.rotationDegrees(lidAngle), 0.0F, 10 / 16.0F, 15 / 16.0F);
    }

    private static void drawLid(PoseStack.Pose pose, VertexConsumer out, TextureAtlasSprite[] sprite, boolean sealed, int light) {
        for (int f = 0; f < FACES.length; f++) {
            float[] q = FACES[f];
            TextureAtlasSprite s = sprite[sealed && f == TOP_FACE ? SEALED_TOP : FACE_TEXTURE[f]];
            float u1 = u(s, q[15]);
            float v1 = v(s, q[16]);
            float u2 = u(s, q[17]);
            float v2 = v(s, q[18]);
            vertex(pose, out, q, 3, u1, v1, light);
            vertex(pose, out, q, 6, u1, v2, light);
            vertex(pose, out, q, 9, u2, v2, light);
            vertex(pose, out, q, 12, u2, v1, light);
        }
    }

    private static float u(TextureAtlasSprite sprite, float px) {
        return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * px / 16.0F;
    }

    private static float v(TextureAtlasSprite sprite, float px) {
        return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * px / 16.0F;
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer out, float[] q, int at, float u, float v, int light) {
        float x = q[at] / 16.0F;
        float y = q[at + 1] / 16.0F;
        float z = q[at + 2] / 16.0F;
        //? if >=1.21 {
        out.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
            .setNormal(pose, q[0], q[1], q[2]);
        //?} else {
        /*out.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(pose.normal(), q[0], q[1], q[2]).endVertex();
        *///?}
    }
}
