package nl.asrr.microservice.webutils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V> implements Map.Entry<K, V> {

    private K key;

    private V value;

    public V setValue(V value) {
        return this.value = value;
    }

}
