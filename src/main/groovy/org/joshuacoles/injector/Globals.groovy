package org.joshuacoles.injector

import com.google.common.base.Predicate
import org.reflections.Reflections

import javax.annotation.Nullable
import java.lang.annotation.Annotation
import java.lang.reflect.Field

import static java.lang.reflect.Modifier.*
import static org.reflections.ReflectionUtils.*

class Globals {
    static class General {
        static final Closure clDelegate = { it }

        static <T> Set<T> set(final collection, final Closure<T> mutator) {
            (collection.inject(new HashSet()) { Set result, curr -> result << mutator(curr); result }) as Set
        }

        static boolean neq(final thing, final Object... tests) { !tests.any { it == thing } }

        static Predicate<Field> predict(final Closure<Boolean> closure) {
            new Predicate<Field>() {
                @Override
                boolean apply(@Nullable Field field) {
                    return closure(field)
                }
            }
        }

        static void ensure(final boolean b, final GString string) {
            if (!b) throw new Exception(string.toString())
        }

        @SuppressWarnings("GroovyAssignabilityCheck")
        static <K, V> Map<K, V> map(
                final Collection collection, final Closure<K> key, final Closure<V> value, final Map seed = [:]) {
            collection.inject(seed) { result, curr -> result[key(curr)] = value(curr); return result } as Map<K, V>
        }

        static <T> Closure<T> clDelegate(final Class<T> ignored = Object) { clDelegate as Closure<T> }
    }

    static class Reflection {
        static final Reflections reflections = new Reflections("")
        static final Predicate<Field> PSF = modifier(PUBLIC, STATIC, FINAL)

        static <T extends Annotation> T annotation(final Class subject, final Class<T> annotation) {
            (T) subject.getAnnotation(annotation)
        }

        static void nonFinal(final Field field) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.modifiers & ~FINAL);
        }

        static <T extends Annotation> Map<Class, T> annotated(final Class<T> annotation) {
            General.map(reflections.getTypesAnnotatedWith(annotation), General.clDelegate, { Class clazz -> clazz.getAnnotation(annotation) }) as Map<Class, T>
        }

        static Predicate<Field> modifier(final int ... mods) {
            { Field suspect -> mods.collect({ int mod -> withModifier(mod).apply(suspect) }).every() } as Predicate<Field>
        }

        static Predicate<Field> type(final Class type) { withType(type) }

        static Predicate interfaces(final Class... _interfaces) {
            General.predict { Field field ->
                final interfaces = _interfaces.toList().toSet()
                Set<Class> accountedFor = []
                superTypes(field.type).each { Class clazz ->
                    //noinspection GroovyAssignabilityCheck
                    accountedFor.addAll(clazz.interfaces.toList().intersect(interfaces - accountedFor))
                }

                return interfaces == accountedFor
            }
        }

        static Map<Class, List<Field>> fieldsByType(final Collection<Field> collection) {
            collection.groupBy { it.type }
        }

        static Set<Field> fields(final Class<?> type, final Predicate<? super Field>... predicates) {
            getFields(type, predicates)
        }

        static superTypes(final Class clazz) { getAllSuperTypes(clazz) }
    }
}
