package com.xkingdark.bob.helpers;

import com.xkingdark.bob.blocks.Blocks;
import com.xkingdark.bob.core.Events;
import com.xkingdark.bob.items.ItemGroups;
import com.xkingdark.bob.items.Items;
import com.xkingdark.bob.network.NetworkHandler;
import com.xkingdark.bob.sound.SoundEvents;

public class Registry {
    public static void initialize() {
        try {
            Class.forName(Items.class.getName());
        } catch (ClassNotFoundException DEFAULT) {};
        try {
            Class.forName(Blocks.class.getName());
        } catch (ClassNotFoundException DEFAULT) {};
        try {
            Class.forName(SoundEvents.class.getName());
        } catch (ClassNotFoundException DEFAULT) {};

        ItemGroups.register();
        Events.register();

        NetworkHandler.registerServerPackets();
        NetworkHandler.registerReceivers();
    };
};