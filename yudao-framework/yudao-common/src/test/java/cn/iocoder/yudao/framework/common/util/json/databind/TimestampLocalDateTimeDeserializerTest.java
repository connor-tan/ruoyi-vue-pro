package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimestampLocalDateTimeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
    }

    @Test
    void deserialize_shouldSupportFormattedString() throws Exception {
        Demo demo = objectMapper.readValue("{\"time\":\"2026-05-01 08:30:00\"}", Demo.class);

        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 30), demo.time);
    }

    @Test
    void deserialize_shouldSupportFormattedStringByDefaultPattern() throws Exception {
        DefaultPatternDemo demo = objectMapper.readValue("{\"time\":\"2026-05-01 08:30:00\"}",
                DefaultPatternDemo.class);

        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 30), demo.time);
    }

    @Test
    void deserialize_shouldSupportEpochMillis() throws Exception {
        LocalDateTime expected = LocalDateTime.of(2026, 5, 1, 8, 30);
        long epochMillis = expected.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Demo demo = objectMapper.readValue("{\"time\":" + epochMillis + "}", Demo.class);

        assertEquals(expected, demo.time);
    }

    @Test
    void deserialize_shouldSupportEpochMillisString() throws Exception {
        LocalDateTime expected = LocalDateTime.of(2026, 5, 1, 8, 30);
        long epochMillis = expected.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Demo demo = objectMapper.readValue("{\"time\":\"" + epochMillis + "\"}", Demo.class);

        assertEquals(expected, demo.time);
    }

    @Test
    void deserialize_shouldRejectInvalidString() {
        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue("{\"time\":\"invalid\"}", Demo.class));
    }

    private static class Demo {

        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        public LocalDateTime time;

    }

    private static class DefaultPatternDemo {

        public LocalDateTime time;

    }

}
