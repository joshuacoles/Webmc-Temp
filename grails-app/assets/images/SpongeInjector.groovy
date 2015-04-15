package webmc

import com.google.common.base.Predicate
import groovy.transform.CompileStatic
import org.apache.commons.lang.IllegalClassException
import org.joshuacoles.webmc.meta.EnumPopulates
import org.joshuacoles.webmc.meta.Populator
import org.reflections.Reflections
import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.annotation.CatalogedBy

import javax.annotation.Nullable
import java.lang.reflect.Field

import static groovy.transform.TypeCheckingMode.SKIP
import static java.lang.reflect.Modifier.*
import static org.reflections.ReflectionUtils.*

@CompileStatic
@SuppressWarnings(["GrUnresolvedAccess", "GroovyAssignabilityCheck"])
class SpongeInjector {

    public static void inject() {
        println "INEJECT 1"
        injectPopulates()
        println "INEJECT  POPLATES DONE"
        injectPopulator()
        println "INEJECT  POPLATOR DONE"
        println "INEJECT DONE"
    }

    private static void injectPopulator() {
        for (type in new Reflections("org.joshuacoles.webmc.sponge").getTypesAnnotatedWith(Populator)) {
            _injectPopulator(type)
        }
    }

    private static void injectPopulates() {
        Reflections reflections = new Reflections("org.joshuacoles.webmc.sponge");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(EnumPopulates);

        println "Found annotion @Populates on $annotated"
        for (it in annotated) {
            println "In populates loop with $it"
            final target = it.getAnnotation(EnumPopulates).value()
            final type = it.getAnnotation(EnumPopulates).type() == EnumPopulates._ ? it.interfaces[0] : it.getAnnotation(EnumPopulates).type()
            if (it.enum) {
                _injectPopulatesEnum(target, type, it as Class<Enum>)
            } else {
                throw new IllegalClassException("Ignoring $type as it is not an enum!!!")
            }
        }
    }

    private static void _injectPopulator(Class container) {
        println "I AM USING POPULATOR ON $container"

        Set<Field> catalogFields = getFields(container,
                withModifier(PUBLIC),
                withModifier(STATIC),
                withModifier(FINAL),
                new Predicate<Field>() {
                    @Override
                    boolean apply(@Nullable Field field) {
                        field.type.interfaces.contains(CatalogType)
                    }
                })

        assert catalogFields != null
        assert !catalogFields.empty

        println "${catalogFields == null}"
        println "FOUDN CTFields $catalogFields"

        Map<Class<? extends CatalogType>, List<Field>> byType =
                (catalogFields.groupBy { Field field -> field.type } as Map<Class<? extends CatalogType>, List<Field>>)

        assert byType != null
        assert !byType.keySet().empty
        assert !byType.values().any { it.empty }

        println "SORTED TO $byType"
        println "SORTED TO ${byType.keySet()}"

        for (type in byType.keySet()) {
            assert type != null
            assert type.interfaces.contains(CatalogType)
//            assert type.annotations*.class.contains(CatalogedBy)
            println "IN LOOP FOR TYPE $type"
            final catalogedBy = type.getAnnotation(CatalogedBy).value()[0]
            assert catalogedBy != null
            final values =
                    byType[type].inject([:]) { LinkedHashMap result, Field it -> result[it.name] = it; return result } as Map

            println "VALUE MAP $values"

            final outputFields = getFields(catalogedBy,
                    withModifier(PUBLIC),
                    withModifier(STATIC),
                    withModifier(FINAL),
                    withType(type))

            println "OUT FIELDS $outputFields"

            if (!(outputFields.collect({ it.name }).toSet() == values.keySet()))
                throw new IllegalArgumentException("Values must be on a 1 to 1 relationship with all the PSF fields of the target class that are instances of $type")

            println "CHECKS OUT!!!!!!!"

            for (field in outputFields) {
                println "IN LOPP FOR TYPE $type AND field $field"
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.modifiers & ~FINAL);

                field.set(catalogedBy, values[field.name])
                println "VALUE SET FOR field $field AND VALUE ${values[field.name]}"
            }
        }
    }

    @CompileStatic(SKIP)
    private static void _injectPopulatesEnum(Class target, Class type, Class<Enum> values) {
        _injectPopulates(target,
                type,
                values.values().inject([:]) { LinkedHashMap result, Enum it -> result[it.name()] = it; return result } as Map)
    }


    private static void _injectPopulates(Class target, Class type, Map<String, ?> values) {
        def s = getFields(target,
                withModifier(PUBLIC),
                withModifier(STATIC),
                withModifier(FINAL),
                withType(type))

        if (!(values.values().every { type.isInstance(it) }))
            throw new IllegalArgumentException("The Collection must only contain instances of $type!")

        if (!((s.collect { it.name }).toSet() == values.keySet()))
            throw new IllegalArgumentException("Values must be on a 1 to 1 relationship with all the PSF fields of the target class that are instances of $type")

        for (field in s) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.modifiers & ~FINAL)

            field.set(type, values[field.name])
        }
    }

}
