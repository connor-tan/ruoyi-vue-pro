package cn.iocoder.yudao.framework.common.util.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsLocalDateTimeFormatTest {

    @Test
    void shouldUseJsonFormatPatternForDirectField() {
        DirectRespVO vo = new DirectRespVO();
        vo.setCreateTime(LocalDateTime.of(2026, 4, 9, 17, 30, 45));

        String json = JsonUtils.toJsonString(vo);
        assertTrue(json.contains("\"createTime\":\"2026-04-09 17:30:45\""), json);
    }

    @Test
    void shouldUseJsonFormatPatternForInheritedField() {
        ChildRespVO vo = new ChildRespVO();
        vo.setStartTime(LocalDateTime.of(2026, 4, 9, 17, 30, 45));

        String json = JsonUtils.toJsonString(vo);
        assertTrue(json.contains("\"startTime\":\"2026-04-09 17:30:45\""), json);
    }

    @Test
    void shouldFallbackToTimestampWithoutJsonFormat() {
        DefaultRespVO vo = new DefaultRespVO();
        vo.setCreateTime(LocalDateTime.of(2026, 4, 9, 17, 30, 45));

        String json = JsonUtils.toJsonString(vo);
        assertTrue(json.matches("\\{\"createTime\":\\d+}"), json);
    }

    @Test
    void shouldDeserializeDigitOnlyStringByJsonFormatPatternBeforeEpochMillisFallback() {
        DigitPatternReqVO vo = JsonUtils.parseObject("{\"createTime\":\"20260409173045\"}", DigitPatternReqVO.class);

        assertEquals(LocalDateTime.of(2026, 4, 9, 17, 30, 45), vo.getCreateTime());
    }

    @Test
    void shouldDeserializeDigitOnlyNumberTokenByJsonFormatPatternBeforeEpochMillisFallback() {
        DigitPatternReqVO vo = JsonUtils.parseObject("{\"createTime\":20260409173045}", DigitPatternReqVO.class);

        assertEquals(LocalDateTime.of(2026, 4, 9, 17, 30, 45), vo.getCreateTime());
    }

    static class DefaultRespVO {
        private LocalDateTime createTime;

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    static class DirectRespVO {
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime createTime;

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    static class ParentRespVO {
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime startTime;

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
    }

    static class ChildRespVO extends ParentRespVO {
    }

    static class DigitPatternReqVO {
        @JsonFormat(pattern = "yyyyMMddHHmmss")
        private LocalDateTime createTime;

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

}
