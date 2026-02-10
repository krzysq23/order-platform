package pl.xsware.orders.infrastructure.config;

import java.io.IOException;
import java.lang.reflect.Type;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.AbstractJsonFormatMapper;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.json.JsonMapper;

public final class JacksonJsonFormatMapper extends AbstractJsonFormatMapper {

    public static final String SHORT_NAME = "jackson";

    private final JsonMapper jsonMapper;

    public JacksonJsonFormatMapper() {
        this(JsonMapper.shared());
    }

    public JacksonJsonFormatMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <T> void writeToTarget(
        T value,
        JavaType<T> javaType,
        Object target,
        WrapperOptions options
    ) throws IOException {

        jsonMapper
            .writerFor(jsonMapper.constructType(javaType.getJavaType()))
            .writeValue((JsonGenerator) target, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readFromSource(
        JavaType<T> javaType,
        Object source,
        WrapperOptions options
    ) throws IOException {

        return (T) jsonMapper.readValue(
            (JsonParser) source,
            jsonMapper.constructType(javaType.getJavaType())
        );
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return JsonParser.class.isAssignableFrom(sourceType);
    }

    @Override
    public boolean supportsTargetType(Class<?> targetType) {
        return JsonGenerator.class.isAssignableFrom(targetType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromString(CharSequence charSequence, Type type) {
        return (T) jsonMapper.readValue(
            charSequence.toString(),
            jsonMapper.constructType(type)
        );
    }

    @Override
    public <T> String toString(T value, Type type) {
        return jsonMapper
            .writerFor(jsonMapper.constructType(type))
            .writeValueAsString(value);
    }
}
