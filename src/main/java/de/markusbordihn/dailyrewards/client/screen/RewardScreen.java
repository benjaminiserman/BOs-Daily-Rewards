/**
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.dailyrewards.client.screen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import de.markusbordihn.dailyrewards.Constants;
import de.markusbordihn.dailyrewards.menu.RewardMenu;
import de.markusbordihn.dailyrewards.rewards.Rewards;

public class RewardScreen extends AbstractContainerScreen<RewardMenu> {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ResourceLocation texture =
      new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen.png");

  private TranslatableComponent rewardScreenTitle;

  public RewardScreen(RewardMenu menu, Inventory inventory, Component component) {
    super(menu, inventory, component);
  }

  @Override
  public void init() {
    super.init();

    // Default stats
    this.imageWidth = 171;
    this.imageHeight = 247;

    // Set Title with already rewarded days.
    int rewardedDays = this.menu.getRewardedDays();
    rewardScreenTitle = new TranslatableComponent(Constants.TEXT_PREFIX + "reward_screen",
        rewardedDays);

    // Set background according the number or days for the current month.
    switch(Rewards.getDaysCurrentMonth()) {
      case 28:
        texture = new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen_28_days.png");
      break;
      case 29:
        texture = new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen_29_days.png");
      break;
      case 30:
        texture = new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen_30_days.png");
      break;
      case 31:
        texture = new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen_31_days.png");
      break;
      default:
        texture = new ResourceLocation(Constants.MOD_ID, "textures/container/reward_screen.png");
    }

    this.titleLabelX = (this.imageWidth - this.font.width(rewardScreenTitle)) / 2;
    this.titleLabelY = 6;
    this.topPos = (this.height - this.imageHeight) / 2;
    this.inventoryLabelX = 6;
    this.inventoryLabelY = this.imageHeight - 90;
  }

  @Override
  public void render(PoseStack poseStack, int x, int y, float partialTicks) {
    this.renderBackground(poseStack);
    super.render(poseStack, x, y, partialTicks);
    this.renderTooltip(poseStack, x, y);
  }

  @Override
  protected void renderLabels(PoseStack poseStack, int x, int y) {
    this.font.draw(poseStack, rewardScreenTitle, this.titleLabelX, this.titleLabelY, 4210752);
    this.font.draw(poseStack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
        4210752);
  }

  @Override
  protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, texture);

    // Main screen
    this.blit(poseStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
  }

}
