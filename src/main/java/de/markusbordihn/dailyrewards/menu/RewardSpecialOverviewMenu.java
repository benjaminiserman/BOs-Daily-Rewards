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

package de.markusbordihn.dailyrewards.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import de.markusbordihn.dailyrewards.Constants;
import de.markusbordihn.dailyrewards.data.RewardData;
import de.markusbordihn.dailyrewards.data.SpecialRewardUserData;
import de.markusbordihn.dailyrewards.menu.slots.EmptyRewardSlot;
import de.markusbordihn.dailyrewards.menu.slots.RewardSlot;
import de.markusbordihn.dailyrewards.rewards.SpecialRewards;

public class RewardSpecialOverviewMenu extends RewardMenu {

  // Define Slot index for easier access
  public static final int PLAYER_SLOT_START = 9;
  public static final int PLAYER_INVENTORY_SLOT_START = PLAYER_SLOT_START;
  public static final int PLAYER_SLOT_STOP = 3 * 9 + PLAYER_INVENTORY_SLOT_START + 8;

  // Defining basic layout options
  private static int containerSize = 32;
  private static int slotSize = 18;

  // Container layout
  public static final float REWARD_SLOT_SIZE_X = 20.5f;
  public static final int REWARD_SLOT_SIZE_Y = 30;
  public static final int REWARD_SLOT_START_POSITION_X = 8;
  public static final int REWARD_SLOT_START_POSITION_Y = 20;

  // Container
  private Container specialRewardsContainer = new SimpleContainer(containerSize);
  private Container specialRewardsUserContainer = new SimpleContainer(containerSize);

  // Rewards Data
  private List<ItemStack> specialRewardsForCurrentMonth = new ArrayList<>();
  private List<ItemStack> specialUserRewardsForCurrentMonth = new ArrayList<>();

  public RewardSpecialOverviewMenu(final int windowId, final Inventory playerInventory) {
    this(windowId, playerInventory, ModMenuTypes.REWARD_SPECIAL_OVERVIEW_MENU.get(),
        playerInventory.player.getUUID(),
        SpecialRewardUserData.get()
            .getRewardedDaysForCurrentMonth(playerInventory.player.getUUID()),
        SpecialRewardUserData.get()
            .getLastRewardedDayForCurrentMonth(playerInventory.player.getUUID()),
        SpecialRewardUserData.get()
            .getRewardsForCurrentMonthSyncData(playerInventory.player.getUUID()),
        RewardData.get().getSpecialRewardsForCurrentMonthSyncData());
  }

  public RewardSpecialOverviewMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
    this(windowId, playerInventory, ModMenuTypes.REWARD_SPECIAL_OVERVIEW_MENU.get(),
        data.readUUID(), data.readInt(), data.readUtf(), data.readNbt(), data.readNbt());
  }

  public RewardSpecialOverviewMenu(final int windowId, final Inventory playerInventory,
      MenuType<?> menuType, UUID playerUUID, int rewardedDays, String lastRewardedDay,
      CompoundTag userRewardsForCurrentMonth, CompoundTag rewardsForCurrentMonth) {
    super(menuType, windowId, playerInventory);

    // Check if we have a valid player with the given UUID
    this.playerUUID = playerUUID;
    if (this.playerUUID == null || this.player.getUUID() == null
        || !this.playerUUID.equals(this.player.getUUID())) {
      log.error("{} Unable to verify player {} with UUID {}!", Constants.LOG_NAME, this.player,
          this.playerUUID);
      return;
    }

    // Store rewards data
    this.rewardedDays = rewardedDays;
    this.lastRewardedDay = lastRewardedDay;

    // Sync possible rewards items for current month.
    this.specialRewardsForCurrentMonth =
        RewardData.getSpecialRewardsForCurrentMonthSyncData(rewardsForCurrentMonth);
    if (!rewardsForCurrentMonth.isEmpty()) {
      for (int index = 0; index < this.specialRewardsForCurrentMonth.size(); index++) {
        this.specialRewardsContainer.setItem(index, this.specialRewardsForCurrentMonth.get(index));
      }
    }

    // Sync user rewarded items for current month.
    this.specialUserRewardsForCurrentMonth =
        SpecialRewardUserData.getRewardsForCurrentMonthSyncData(userRewardsForCurrentMonth);
    if (!this.specialUserRewardsForCurrentMonth.isEmpty()) {
      for (int index = 0; index < this.specialUserRewardsForCurrentMonth.size(); index++) {
        this.specialRewardsUserContainer.setItem(index,
            this.specialUserRewardsForCurrentMonth.get(index));
      }
    }

    // Define Rewards Slots and render takeable rewards, upcoming rewards and empty rewards.
    int numberOfDays = SpecialRewards.getDaysCurrentMonth();
    for (int rewardRow = 0; rewardRow < 4; ++rewardRow) {
      for (int rewardColumn = 0; rewardColumn < 8; ++rewardColumn) {
        int rewardSlotIndex = rewardColumn + rewardRow * 8;
        if (this.specialUserRewardsForCurrentMonth.size() > rewardSlotIndex
            && this.specialUserRewardsForCurrentMonth.get(rewardSlotIndex) != null
            && !this.specialUserRewardsForCurrentMonth.get(rewardSlotIndex).isEmpty()) {
          ItemStack itemStack;
          try {
            itemStack = this.specialUserRewardsForCurrentMonth.get(rewardSlotIndex);
          } catch (Exception e) {
            itemStack = ItemStack.EMPTY;
          }
          this.addSlot(this.createRewardSlot(itemStack, rewardedDays,
              this.specialRewardsUserContainer, rewardSlotIndex,
              REWARD_SLOT_START_POSITION_X + Math.round(rewardColumn * REWARD_SLOT_SIZE_X),
              REWARD_SLOT_START_POSITION_Y + rewardRow * REWARD_SLOT_SIZE_Y));
        } else if (this.specialRewardsForCurrentMonth.size() > rewardSlotIndex) {
          // If the reward was not taken yet show the reward slot.
          this.addSlot(new RewardSlot(this.specialRewardsContainer, rewardSlotIndex,
              REWARD_SLOT_START_POSITION_X + Math.round(rewardColumn * REWARD_SLOT_SIZE_X),
              REWARD_SLOT_START_POSITION_Y + rewardRow * REWARD_SLOT_SIZE_Y, this));
        } else if (numberOfDays > rewardSlotIndex) {
          // If there is no reward show the empty reward slot.
          this.addSlot(new EmptyRewardSlot(this.specialRewardsContainer, rewardSlotIndex,
              REWARD_SLOT_START_POSITION_X + Math.round(rewardColumn * REWARD_SLOT_SIZE_X),
              REWARD_SLOT_START_POSITION_Y + rewardRow * REWARD_SLOT_SIZE_Y, this));
        }
      }
    }

    // Player Inventory Slots
    int playerInventoryStartPositionY = 160;
    int playerInventoryStartPositionX = 8;
    for (int inventoryRow = 0; inventoryRow < 3; ++inventoryRow) {
      for (int inventoryColumn = 0; inventoryColumn < 9; ++inventoryColumn) {
        this.addSlot(new Slot(playerInventory, inventoryColumn + inventoryRow * 9 + 9,
            playerInventoryStartPositionX + inventoryColumn * slotSize,
            playerInventoryStartPositionY + inventoryRow * slotSize));
      }
    }

    // Player Hotbar Slots
    int hotbarStartPositionY = 218;
    int hotbarStartPositionX = 8;
    for (int playerInventorySlot = 0; playerInventorySlot < 9; ++playerInventorySlot) {
      this.addSlot(new Slot(playerInventory, playerInventorySlot,
          hotbarStartPositionX + playerInventorySlot * slotSize, hotbarStartPositionY));
    }
  }

  @Override
  public Container getSpecialRewardsContainer() {
    return this.specialRewardsContainer;
  }

  @Override
  public Container getSpecialRewardsUserContainer() {
    return this.specialRewardsUserContainer;
  }

  @Override
  public List<ItemStack> getSpecialRewardsForCurrentMonth() {
    return this.specialRewardsForCurrentMonth;
  }

  @Override
  public List<ItemStack> getUserSpecialRewardsForCurrentMonth() {
    return this.specialUserRewardsForCurrentMonth;
  }

}
