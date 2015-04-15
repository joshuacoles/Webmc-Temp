package org.joshuacoles.webmc.sponge

import groovy.transform.Immutable
import org.joshuacoles.webmc.meta.Populator
import org.spongepowered.api.world.difficulty.Difficulty

@Immutable
@Newify(QDifficult)
@Populator
class QDifficult implements Difficulty {
    public static final Difficulty EASY = QDifficult("EASY", "EASY")
    public static final Difficulty NORMAL = QDifficult("NORMAL", "NORMAL")
    public static final Difficulty HARD = QDifficult("HARD", "HARD")
    public static final Difficulty PEACEFULL = QDifficult("PEACEFULL", "PEACEFULL")

    String id,
           name = id

}
