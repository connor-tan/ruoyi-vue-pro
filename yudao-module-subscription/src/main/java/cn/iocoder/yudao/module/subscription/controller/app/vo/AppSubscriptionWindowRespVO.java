package cn.iocoder.yudao.module.subscription.controller.app.vo;

import lombok.Data;

@Data
public class AppSubscriptionWindowRespVO {

    private Window window;
    private Student student;
    private String blockedReason;
    private String blockedReasonDesc;

    @Data
    public static class Window {
        private Long id;
        private String name;
        private Long targetYearCatalogId;
        private String targetYearNameSnapshot;
        private Integer targetYearStart;
        private Integer targetYearEnd;
    }

    @Data
    public static class Student {
        private Long studentId;
        private String studentName;
        private Long schoolId;
        private String schoolName;
        private Long classId;
        private String className;
        private Long gradeCatalogId;
        private String gradeName;
        private String gradeResolveSource;
        private Long stationId;
        private String stationName;
        private String blockedReason;
        private String blockedReasonDesc;
    }

}
