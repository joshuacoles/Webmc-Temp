package org.joshuacoles.webmc.meta

import java.lang.reflect.Field

@SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
class ReflectionExtension {
    void setModifiers(Field self, int value) {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(self, value);
    }

    void call(Field self, object, value) {
        self.set(object, value) //todo do we need to call the specific setXXX() methods?
    }
}
