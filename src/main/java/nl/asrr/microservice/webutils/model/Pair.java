package nl.asrr.microservice.webutils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V> {

    private K key;

    private V value;

}
