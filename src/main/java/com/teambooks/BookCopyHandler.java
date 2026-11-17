package com.teambooks;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BookCopyHandler {
    private static final int COST_ENCHANTED_BOOK = 5;
    private static final int COST_WRITTEN_BOOK = 3;
    private static final int COST_WRITABLE_BOOK = 3;

    private BookCopyHandler() {
    }

    public static InteractionResult handle(Player player, net.minecraft.world.level.Level level,
                                           InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof EnchantingTableBlock)) {
            return InteractionResult.PASS;
        }

        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() != Items.BOOK) {
            return InteractionResult.PASS;
        }

        ItemStack source = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack copy = makeCopy(source);
        if (copy == null) {
            return InteractionResult.PASS;
        }

        int cost = copyCost(source);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        if (serverPlayer.experienceLevel < cost) {
            serverPlayer.sendSystemMessage(Component.literal("\u00a7cНедостаточно опыта для копирования книги."));
            return InteractionResult.FAIL;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, copy);
        chargeLevels(serverPlayer, cost);
        return InteractionResult.SUCCESS;
    }

    private static ItemStack makeCopy(ItemStack source) {
        if (source.getItem() == Items.ENCHANTED_BOOK) {
            ItemEnchantments stored = source.get(DataComponents.STORED_ENCHANTMENTS);
            ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
            if (stored != null && !stored.isEmpty()) {
                out.set(DataComponents.STORED_ENCHANTMENTS, stored);
            }
            return out;
        }
        if (source.getItem() == Items.WRITTEN_BOOK) {
            WrittenBookContent content = source.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                return null;
            }
            ItemStack out = new ItemStack(Items.WRITTEN_BOOK);
            out.set(DataComponents.WRITTEN_BOOK_CONTENT, content.craftCopy());
            return out;
        }
        if (source.getItem() == Items.WRITABLE_BOOK) {
            WritableBookContent content = source.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) {
                return null;
            }
            ItemStack out = new ItemStack(Items.WRITABLE_BOOK);
            out.set(DataComponents.WRITABLE_BOOK_CONTENT, content);
            return out;
        }
        return null;
    }

    private static int copyCost(ItemStack source) {
        if (source.getItem() == Items.ENCHANTED_BOOK) {
            return COST_ENCHANTED_BOOK;
        }
        if (source.getItem() == Items.WRITTEN_BOOK) {
            return COST_WRITTEN_BOOK;
        }
        return COST_WRITABLE_BOOK;
    }

    private static void chargeLevels(ServerPlayer player, int levels) {
        int next = Math.max(0, player.experienceLevel - levels);
        player.setExperienceLevels(next);
        player.experienceProgress = 0.0f;
    }
}
