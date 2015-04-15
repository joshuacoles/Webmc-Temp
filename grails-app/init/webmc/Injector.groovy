package webmc

import com.google.common.base.Predicate
import org.joshuacoles.webmc.NonOverridableHashMap
import org.joshuacoles.webmc.meta.EnumPopulates
import org.joshuacoles.webmc.meta.Populator
import org.reflections.Reflections
import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.annotation.CatalogedBy

import javax.annotation.Nullable
import java.lang.annotation.Annotation
import java.lang.reflect.Field

import static java.lang.reflect.Modifier.*
import static org.reflections.ReflectionUtils.getAllSuperTypes as superTypes
import static org.reflections.ReflectionUtils.getFields as fields
import static org.reflections.ReflectionUtils.withModifier
import static org.reflections.ReflectionUtils.withType
import static webmc.Injector.Helper.*

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
        }, { Map.Entry<Class, Field> entry -> [entry.value.name, entry.value.get(entry.value.declaringClass) }, new NonOverridableHashMap()) as Map<Class, Map<String, Object>>
        
    }

    private static boolean leq(List<String> a, List<String> b) {
        // Check for sizes and nulls
        if ((a.size() != b.size()) || (a == null && b != null) || (a != null && b == null)) {
            return false;
        }

        if (a == null && b == null) return true;

        // Sort and compare the two lists
        Collections.sort(a);
        Collections.sort(b);
        return a.equals(b);
    }

    static class Helper {
        private static final Reflections reflections = new Reflections("org.joshuacoles.webmc.sponge")
        private static final Closure clDelegate = { it }
        private static final Predicate<Field> PSF = modifier(PUBLIC, STATIC, FINAL)

        private static boolean eq(thing, Object... tests) { tests.every { it == thing } }

        private static boolean neq(thing, Object... tests) { !tests.any { it == thing } }

        private static <T extends Annotation> T annotation(Class subject, Class<T> annotation) {
            subject.getAnnotation(annotation)
        }

        private static Set set(collection, Closure mutator) {
            (collection.inject(new HashSet()) { result, curr -> mutator(result, curr); result }) as Set
        }

        private static Predicate<Field> predict(Closure<Boolean> closure) {
            new Predicate<Field>() {
                @Override
                boolean apply(@Nullable Field field) {
                    return closure(field)
                }
            }
        }

        private static void nonFinal(Field field) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.modifiers & ~FINAL);
        }


        private static void ensure(boolean b, GString string) {
            if (!b) throw new Exception(string.toString())
        }

        private static <T extends Annotation> Map<Class, T> annotated(Class<T> annotation) {
            map(reflections.getTypesAnnotatedWith(annotation), clDelegate, { Class clazz -> clazz.getAnnotation(annotation) }) as Map<Class, T>
        }

        @SuppressWarnings("GroovyAssignabilityCheck")
        private static <K, V> Map<K, V> map(Collection collection, Closure<K> key, Closure<V> value, Map seed = [:]) {
            collection.inject(seed) { result, curr -> result[key(curr)] = value(curr); return result } as Map<K, V>
        }

        @SuppressWarnings("GroovyAssignabilityCheck")
        private static <K, V> Map<K, V> map(Map map, Closure<K> key, Closure<V> value, Map seed = [:]) {
            map.inject(seed) { result, curr -> result[key(curr)] = value(curr); return result } as Map<K, V>
        }

        private static boolean n(suspect) { suspect == null }

        private static Predicate<Field> modifier(final int ... mods) {
            { Field suspect -> mods.collect({ int mod -> withModifier(mod).apply(suspect) }).every() } as Predicate<Field>
        }

        private static Predicate<Field> type(Class type) { withType(type) }

        private static Predicate interfaces(Class... _interfaces) {
            predict { Field field ->
                final interfaces = _interfaces.toList().toSet()
                Set<Class> accountedFor = []
                superTypes(field.type).each { Class clazz ->
                    //noinspection GroovyAssignabilityCheck
                    accountedFor.addAll(clazz.interfaces.toList().intersect(interfaces - accountedFor))
                }

                return interfaces == accountedFor
            }
        }

        private static Map<Class, List<Field>> fieldsByType(Collection<Field> collection) {
            collection.groupBy { it.type }
        }

        private static <T> Closure<T> clDelegate(Class<T> ignored = Object) { clDelegate as Closure<T> }
    }
}
