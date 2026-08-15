package ar1hurgit.pearlsplus.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class PearlsPlusConfigScreen extends Screen {
    private static final double MIN_SCALE = 0.1D;
    private static final double MAX_SCALE = 2.0D;
    private static final double MIN_DISTANCE = 8.0D;
    private static final double MAX_DISTANCE = 256.0D;

    private final Screen parent;
    private final PearlsPlusConfig config;

    public PearlsPlusConfigScreen(Screen parent) {
        super(Text.literal("Pearls+ Config"));
        this.parent = parent;
        this.config = PearlsPlusConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 40;

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showNametag)
                .build(centerX - 100, y, 200, 20, Text.literal("Show Nametag"),
                        (btn, value) -> config.showNametag = value));

        y += 24;
        this.addDrawableChild(new ScaleSlider(centerX - 100, y, 200, 20, config.nametagScale));

        y += 24;
        this.addDrawableChild(new DistanceSlider(centerX - 100, y, 200, 20, config.maxDistance));

        y += 30;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> this.close())
                .dimensions(centerX - 100, y, 200, 20).build());
    }

    @Override
    public void close() {
        config.save();
        this.client.setScreen(parent);
    }

    private class ScaleSlider extends SliderWidget {
        ScaleSlider(int x, int y, int width, int height, double initialScale) {
            super(x, y, width, height, Text.empty(), (initialScale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE));
            this.updateMessage();
        }

        private float currentScale() {
            return (float) (MIN_SCALE + this.value * (MAX_SCALE - MIN_SCALE));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(String.format("Nametag Scale: %.2f", currentScale())));
        }

        @Override
        protected void applyValue() {
            config.nametagScale = currentScale();
        }
    }

    private class DistanceSlider extends SliderWidget {
        DistanceSlider(int x, int y, int width, int height, double initialDistance) {
            super(x, y, width, height, Text.empty(), (initialDistance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE));
            this.updateMessage();
        }

        private double currentDistance() {
            return MIN_DISTANCE + this.value * (MAX_DISTANCE - MIN_DISTANCE);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(String.format("Max Distance: %.0f blocks", currentDistance())));
        }

        @Override
        protected void applyValue() {
            config.maxDistance = currentDistance();
        }
    }
}
