// PearlNameClient.java
package ar1hurgit.pearlsplus.client;

import ar1hurgit.pearlsplus.client.config.PearlsPlusConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.state.FlyingItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PearlNameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                net.minecraft.entity.EntityType.ENDER_PEARL,
                EnderPearlWithNameRenderer::new
        );
    }

    /**
     * Custom render state carrying the owner's name separately from the
     * vanilla {@code displayName} field. This is deliberate: EntityRenderer's
     * own render() already calls renderLabelIfPresent() automatically once
     * displayName/nameLabelPos are set, so we keep those null during the
     * automatic pass and only populate them for our own single, scaled
     * label draw below - otherwise you get two overlapping nametags.
     */
    public static class PearlRenderState extends FlyingItemEntityRenderState {
        public Text ownerLabel;
    }

    public static class EnderPearlWithNameRenderer extends FlyingItemEntityRenderer<EnderPearlEntity> {
        /**
         * Owner-name cache keyed by the pearl's own UUID. pearl.getOwner()
         * only resolves if the client currently has the owning player's
         * entity tracked - if that player briefly drops out of tracking
         * range (or disconnects/relogs), getOwner() returns null even
         * though the pearl is still in flight, which made the nametag
         * flicker away. We cache the name the first time it resolves so
         * it survives the owner entity being (temporarily) untracked.
         * Bounded with a simple LRU eviction so it can't grow unbounded
         * on long sessions.
         */
        private static final int MAX_CACHE_SIZE = 200;
        private static final Map<UUID, String> OWNER_NAME_CACHE = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, String> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        public EnderPearlWithNameRenderer(EntityRendererFactory.Context context) {
            super(context);
        }

        @Override
        public FlyingItemEntityRenderState createRenderState() {
            return new PearlRenderState();
        }

        @Override
        public void updateRenderState(EnderPearlEntity pearl, FlyingItemEntityRenderState state, float tickDelta) {
            super.updateRenderState(pearl, state, tickDelta);

            UUID pearlId = pearl.getUuid();
            String name;
            if (pearl.getOwner() != null) {
                name = pearl.getOwner().getName().getString();
                OWNER_NAME_CACHE.put(pearlId, name);
            } else {
                name = OWNER_NAME_CACHE.get(pearlId);
            }

            ((PearlRenderState) state).ownerLabel = name != null ? Text.literal(name) : null;
        }

        @Override
        public void render(FlyingItemEntityRenderState state, MatrixStack matrices,
                            OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
            super.render(state, matrices, queue, cameraState);

            PearlsPlusConfig config = PearlsPlusConfig.get();
            if (!config.showNametag) return;

            Text ownerLabel = ((PearlRenderState) state).ownerLabel;
            if (ownerLabel == null) return;

            double maxDistSq = config.maxDistance * config.maxDistance;
            if (state.squaredDistanceToCamera > maxDistSq) return;

            state.displayName = ownerLabel;
            state.nameLabelPos = new Vec3d(0.0D, state.height + 0.5D, 0.0D);

            matrices.push();
            matrices.translate(0.0D, 0.1D, 0.0D);
            matrices.scale(config.nametagScale, config.nametagScale, config.nametagScale);
            this.renderLabelIfPresent(state, matrices, queue, cameraState);
            matrices.pop();
        }
    }
}
