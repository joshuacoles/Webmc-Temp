package org.joshuacoles.injector

import groovy.transform.CompileStatic
import org.joshuacoles.webmc.meta.EnumPopulates
import org.joshuacoles.webmc.meta.Populator
import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.annotation.CatalogedBy

import java.lang.reflect.Field

import static org.joshuacoles.injector.Globals.General.*
import static org.joshuacoles.injector.Globals.Reflection.*

/**
 * Handles all injection done by the {@code EnumPopulates} and the {@code Populator} annotations.
 * */
@CompileStatic
class Injector {
    /**
     * Entry point for injection. To be called when values are required to be injected
     * */
    static inject() {
        injectEnumPopulates()
        injectPopulator()
    }

    /**
     * Injects the {@code EnumPopulates} annotation.
     * */
    private static injectEnumPopulates() {
        final annotated = annotated(EnumPopulates)

        annotated.each { clazz, annotation ->
            ensure(clazz.enum, "The class $clazz must be an Enum to be used with @EnumPopulates")
            final target = annotation.value()
            final endType = neq(annotation.type(), null, EnumPopulates._) ? annotation.type() : clazz.interfaces[0]

            assert endType != EnumPopulates._

            final populatingFields = map((clazz.getDeclaredMethod("values", new Class[0]).invoke(clazz, new Object[0])) as Collection<Enum>, { Enum it -> it.name() }, clDelegate(Enum)) as Map<String, Enum> //todo FIX THIS WITH COMPILE STATIC
            final catalogFields = fields(target, Globals.Reflection.PSF, type(endType))

            ensure !catalogFields.empty, "Does the $target have the required PSF fields of the type $endType?"

            ensure(catalogFields*.name.toSet() == populatingFields.keySet(),
                    "The members of the $clazz must match up by name with those of the taget class $target.")

            for (catalogField in catalogFields) {
                nonFinal(catalogField)
                catalogField.set(target, populatingFields[catalogField.name])
            }
        }
    }

    /**
     * Injects the {@code Populator} annotation.
     * */
    private static injectPopulator() {
        final populators = annotated(Populator)

        if (populators?.isEmpty()) return

        final populatingFields = populators.keySet().collect {
            fields(it, PSF, interfaces(CatalogType))
        }.sum() as Set<Field>

        if (populatingFields?.empty) return

        final catalogsByType = map(fieldsByType(populatingFields).keySet(), clDelegate, { Class it ->
            annotation(it, CatalogedBy).value()
        }) as Map<Class, Class[]>

        fieldsByType(populatingFields).each { consideredType, knownFields ->
            final catalogFields = set(catalogsByType[consideredType].toList(), { Class clazz -> fields(clazz, PSF, type(consideredType)) }).sum() as Set<Field>
            knownFields.each { popField ->
                println popField.name
                final field = (catalogFields.find { it.name == popField.name })
                nonFinal(field)
                field.set(field.declaringClass, popField.get(popField.declaringClass))
            }
        }
    }
}
