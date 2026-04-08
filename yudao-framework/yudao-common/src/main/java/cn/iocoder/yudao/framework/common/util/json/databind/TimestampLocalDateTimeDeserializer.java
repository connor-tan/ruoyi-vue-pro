package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 基于时间戳的 LocalDateTime 反序列化器
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
        implements ContextualDeserializer {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer(null);

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final String pattern;

    public TimestampLocalDateTimeDeserializer() {
        this(null);
    }

    private TimestampLocalDateTimeDeserializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), ZoneId.systemDefault());
        }

        String text = p.getValueAsString();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        if (StrUtil.isNumeric(text)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault());
        }

        LocalDateTime parsed = tryParse(text, pattern);
        if (parsed != null) {
            return parsed;
        }
        parsed = tryParse(text, DEFAULT_PATTERN);
        if (parsed != null) {
            return parsed;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ex) {
            throw InvalidFormatException.from(p,
                    String.format("无法解析 LocalDateTime：%s", text),
                    text,
                    LocalDateTime.class);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return this;
        }
        JsonFormat jsonFormat = property.getAnnotation(JsonFormat.class);
        if (jsonFormat == null) {
            jsonFormat = property.getContextAnnotation(JsonFormat.class);
        }
        if (jsonFormat == null || StrUtil.isBlank(jsonFormat.pattern())) {
            return this;
        }
        return new TimestampLocalDateTimeDeserializer(jsonFormat.pattern());
    }

    private LocalDateTime tryParse(String text, String pattern) {
        if (StrUtil.isBlank(pattern)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

}
