package com.xkingdark.bob.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class BaneSpikeItem extends Item {
    public BaneSpikeItem(Item.Settings settings) {
        super(settings.sword(new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1704, 5.0F, 2.0F, 8,
            null
        ), 7.0F, -2.4F));
    };

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(this.translationKey)
            .setStyle(Style.EMPTY.withColor(
                Integer.parseInt("DEB12D", 16)
            ));
    };

    @Override
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        textConsumer.accept(this.getDescription().formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    };

    public MutableText getDescription() {
        return Text.translatable(this.translationKey + ".desc");
    };

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);

        World world = attacker.getEntityWorld();
        int TicksPerSecond = MathHelper.floor(world.getTickManager().getTickRate());
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.POISON, 5 * TicksPerSecond, 1)
        );
    };
};