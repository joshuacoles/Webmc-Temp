package org.joshuacoles.webmc.sponge

import org.joshuacoles.webmc.meta.EnumPopulates
import org.joshuacoles.webmc.meta.Populator
import org.spongepowered.api.world.difficulty.Difficulties
import org.spongepowered.api.world.difficulty.Difficulty

@EnumPopulates(Difficulties)
//@Populator
enum _Difficulty implements Difficulty {
    PEACEFUL,
    EASY,
    NORMAL,
    HARD

    final String name
    final String id

    _Difficulty() {
        name = name().toLowerCase().capitalize()
        id = name()
    }
}