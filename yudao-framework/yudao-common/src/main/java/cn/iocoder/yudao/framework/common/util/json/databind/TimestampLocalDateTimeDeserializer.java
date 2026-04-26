package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 基于时间戳的 LocalDateTime 反序列化器
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> implements ContextualDeserializer {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private final DateTimeFormatter formatter;

    public TimestampLocalDateTimeDeserializer() {
        this(DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
    }

    private TimestampLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 情况一：兼容默认 Long 时间戳
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return ofEpochMilli(p.getValueAsLong());
        }

        // 情况二：兼容前端 value-format="YYYY-MM-DD HH:mm:ss" 提交的字符串
        String text = p.getValueAsString();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        if (NumberUtil.isLong(text)) {
            return ofEpochMilli(Long.parseLong(text));
        }
        try {
            return LocalDateTime.parse(text, formatter);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException ignored) {
                throw JsonMappingException.from(p, "LocalDateTime 格式不正确：" + text, ex);
            }
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        JsonFormat jsonFormat = property.getAnnotation(JsonFormat.class);
        if (jsonFormat == null || StrUtil.isBlank(jsonFormat.pattern())) {
            return this;
        }
        return new TimestampLocalDateTimeDeserializer(DateTimeFormatter.ofPattern(jsonFormat.pattern()));
    }

    private LocalDateTime ofEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

}
