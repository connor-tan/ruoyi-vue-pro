package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 基于时间戳的 LocalDateTime 序列化器
 *
 * @author 老五
 */
@Slf4j
public class TimestampLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> implements ContextualSerializer {

    public static final TimestampLocalDateTimeSerializer INSTANCE = new TimestampLocalDateTimeSerializer();

    private final DateTimeFormatter formatter;

    public TimestampLocalDateTimeSerializer() {
        this(null);
    }

    private TimestampLocalDateTimeSerializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (formatter != null) {
            gen.writeString(formatter.format(value));
            return;
        }
        gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return this;
        }
        JsonFormat.Value jsonFormat = property.findPropertyFormat(serializers.getConfig(), property.getType().getRawClass());
        if (jsonFormat == null || StrUtil.isBlank(jsonFormat.getPattern())) {
            return this;
        }
        try {
            return new TimestampLocalDateTimeSerializer(DateTimeFormatter.ofPattern(jsonFormat.getPattern()));
        } catch (Exception ex) {
            log.warn("[createContextual][({}#{}) 使用 JsonFormat pattern({}) 失败，尝试使用默认的 Long 时间戳]",
                    property.getMember().getDeclaringClass().getName(), property.getName(), jsonFormat.getPattern(), ex);
            return this;
        }
    }

}
