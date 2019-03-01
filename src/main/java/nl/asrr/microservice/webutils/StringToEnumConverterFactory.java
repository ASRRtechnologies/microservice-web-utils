package nl.asrr.microservice.webutils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;

public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    private static class StringToEnumConverter<E extends Enum<?>> implements Converter<String, E> {

        private Class<E> enumType;

        private StringToEnumConverter(Class<E> enumType) {
            this.enumType = enumType;
        }

        public E convert(@NonNull String source) {
            String enumString = source.replaceAll("-", "_").toUpperCase();
            for (E enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equals(enumString)) {
                    return enumConstant;
                }
            }

            throw new IllegalArgumentException(source);
        }
    }

    @NonNull
    @Override
    public <E extends Enum<?>> Converter<String, E> getConverter(@NonNull Class<E> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

}
