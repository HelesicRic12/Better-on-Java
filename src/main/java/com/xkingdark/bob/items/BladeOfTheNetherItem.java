package com.xkingdark.bob.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BladeOfTheNetherItem extends Item {
    public BladeOfTheNetherItem(Item.Settings settings) {
        super(settings.sword(new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1569, 5.0F, 2.0F, 8,
            null
        ), 12.0F, -2.4F));
    };

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);

        World world = attacker.getEntityWorld();
        int TicksPerSecond = MathHelper.floor(world.getTickManager().getTickRate());
        target.setOnFireForTicks(6 * TicksPerSecond);
    };
};