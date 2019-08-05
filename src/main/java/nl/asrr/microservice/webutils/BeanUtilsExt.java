package nl.asrr.microservice.webutils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;

public class BeanUtilsExt {

    /**
     * Copies every non-null field from the source object to the target object.
     *
     * @param src    the source object
     * @param target the target object
     */
    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    private static String[] getNullPropertyNames(Object source) {
        var src = new BeanWrapperImpl(source);
        var propertyDescriptors = src.getPropertyDescriptors();

        var emptyNames = new HashSet<String>();
        for (var propertyDescriptor : propertyDescriptors) {
            var value = src.getPropertyValue(propertyDescriptor.getName());
            if (value == null) {
                emptyNames.add(propertyDescriptor.getName());
            }
        }
        var result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
