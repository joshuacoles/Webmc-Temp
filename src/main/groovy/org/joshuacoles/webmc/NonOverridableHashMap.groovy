package org.joshuacoles.webmc

import groovy.transform.CompileStatic

@SuppressWarnings("GroovyUncheckedAssignmentOfMemberOfRawType")
@CompileStatic
class NonOverridableHashMap<K, V> extends HashMap<K, V> {
    @Override
    void putAll(Map<? extends K, ? extends V> map) {
        final keys = map.keySet()
        if (keys.any { super.containsKey(it) }) {
            throw new Exception("Cannot perform operation putAll on $this as the map $map confilts on keys ${keys.findAll {super.containsKey(it)}}")
        } else {
            super.putAll(map)
        }
    }

    V putAt(K k, V v) {
        if (super.containsKey(k)) {
            throw new Exception("Cannot reassign the value $v to the key $k which currently conatains ${super[k]}")
        } else {
            return super.putAt(k, v)
        }
    }

    @Override
    V put(K k, V v) {
        if (super.containsKey(k)) {
            throw new Exception("Cannot reassign the value $v to the key $k which currently conatains ${super[k]}")
        } else {
            super.put(k, v)
        }
    }
}
