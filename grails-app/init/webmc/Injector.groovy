package webmc

import org.joshuacoles.webmc.NonOverridableHashMap
import org.joshuacoles.webmc.meta.EnumPopulates
import org.joshuacoles.webmc.meta.Populator
import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.annotation.CatalogedBy

import java.lang.reflect.Field

import static org.joshuacoles.webmc.Globals.General.*
import static org.joshuacoles.webmc.Globals.Reflection.*

class Injector {
    static inject() {
        injectEnumPopulates()
        injectPopulator()
    }

    //todo is this entire thing needed
    private static injectEnumPopulates() {
        final annotated = annotated(EnumPopulates)

        annotated.each { clazz, annotation ->
            ensure(clazz.enum, "The class $clazz must be an Enum to be used with @EnumPopulates")
            final target = annotation.value()
            final endType = neq(annotation.type(), null, EnumPopulates._) ? annotation.type() : clazz.interfaces[0]

            assert endType != EnumPopulates._

//            noinspection GrUnresolvedAccess
            final populatingFields = map((clazz as Class<Enum>).values() as Collection<Enum>, { Enum it -> it.name() }, clDelegate(Enum)) as Map<String, Enum>
            final catalogFields = fields(target, PSF, type(endType))

            ensure !catalogFields.empty, "Does the $target have the required PSF fields of the type $endType?"

            ensure(catalogFields.name.toSet() == populatingFields.keySet(),
                    "The members of the $clazz must match up by name with those of the taget class $target." +
                            " CF${catalogFields.name.toSet()} PF${populatingFields.keySet()}")

            for (catalogField in catalogFields) {
                nonFinal(catalogField)
                catalogField.set(target, populatingFields[catalogField.name])
            }
        }
    }

    private static injectPopulator() {
        final populators = annotated(Populator)

        final populatingFields = populators.keySet().collect {
            fields(it, PSF, interfaces(CatalogType))
        }.sum() as Set<Field>

        final byType = fieldsByType(populatingFields)
        final types = byType.keySet()
        final catalogsByType = types.collect { it.getAnnotation(CatalogedBy).value() }
        final Map<Class, Map<String, Object>> byTypeWithName = map(byType, {
            it.key
        }, { Map.Entry<Class, Field> entry -> [entry.value.name, entry.value.get(entry.value.declaringClass)] }, new NonOverridableHashMap()) as Map<Class, Map<String, Object>>

    }
}
