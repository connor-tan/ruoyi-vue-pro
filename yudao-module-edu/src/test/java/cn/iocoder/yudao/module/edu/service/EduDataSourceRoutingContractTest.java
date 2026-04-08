package cn.iocoder.yudao.module.edu.service;

import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskPageReqVO;
import cn.iocoder.yudao.module.edu.service.school.SchoolServiceImpl;
import cn.iocoder.yudao.module.edu.service.student.StudentPromotionServiceImpl;
import cn.iocoder.yudao.module.edu.service.student.StudentPromotionTaskServiceImpl;
import cn.iocoder.yudao.module.edu.service.student.StudentServiceImpl;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.dynamic.datasource.annotation.Slave;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EduDataSourceRoutingContractTest {

    @Test
    void readMethodsShouldFallBackToDefaultMaster() throws NoSuchMethodException {
        assertMethodDoesNotHaveAnnotation(SchoolServiceImpl.class, Slave.class, "getSchoolSimpleList");
        assertMethodDoesNotHaveAnnotation(SchoolServiceImpl.class, Slave.class, "getGradeCatalogList");
        assertMethodDoesNotHaveAnnotation(StudentServiceImpl.class, Slave.class,
                "getStudentPage", StudentPageReqVO.class);
        assertMethodDoesNotHaveAnnotation(StudentPromotionTaskServiceImpl.class, Slave.class,
                "getPromotionYearOptions");
        assertMethodDoesNotHaveAnnotation(StudentPromotionTaskServiceImpl.class, Slave.class,
                "getPromotionTaskPage", StudentPromotionTaskPageReqVO.class);
        assertMethodDoesNotHaveAnnotation(StudentPromotionTaskServiceImpl.class, Slave.class,
                "getPromotionBatchListByTaskId", Long.class);
        assertMethodDoesNotHaveAnnotation(StudentPromotionTaskServiceImpl.class, Slave.class,
                "getStudentFlowPage", StudentFlowPageReqVO.class);
    }

    @Test
    void writeMethodsShouldRemainPinnedToMaster() throws NoSuchMethodException {
        assertMethodHasAnnotation(StudentPromotionServiceImpl.class, Master.class,
                "executeStudentPromotion", StudentPromotionExecuteReqVO.class);
        assertMethodHasAnnotation(StudentPromotionServiceImpl.class, Master.class,
                "executeStudentPromotion", StudentPromotionExecuteReqVO.class, Long.class);
        assertMethodHasAnnotation(StudentPromotionTaskServiceImpl.class, Master.class,
                "executeGlobalStudentPromotion", StudentGlobalPromotionExecuteReqVO.class);
        assertMethodHasAnnotation(StudentPromotionTaskServiceImpl.class, Master.class,
                "rollbackGlobalStudentPromotion", StudentGlobalPromotionRollbackReqVO.class);
    }

    private static void assertMethodDoesNotHaveAnnotation(Class<?> targetClass,
                                                          Class<? extends Annotation> annotationClass,
                                                          String methodName,
                                                          Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
        assertNull(method.getAnnotation(annotationClass),
                () -> targetClass.getSimpleName() + "#" + methodName + " should not declare @"
                        + annotationClass.getSimpleName());
    }

    private static void assertMethodHasAnnotation(Class<?> targetClass,
                                                  Class<? extends Annotation> annotationClass,
                                                  String methodName,
                                                  Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
        assertNotNull(method.getAnnotation(annotationClass),
                () -> targetClass.getSimpleName() + "#" + methodName + " should declare @"
                        + annotationClass.getSimpleName());
    }

}
