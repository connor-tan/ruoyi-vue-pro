package cn.iocoder.yudao.module.edu.service.student.bo;

import lombok.Data;

/**
 * 待入学学生激活结果。
 */
@Data
public class StudentWaitingEntryActivateRespBO {

    private Integer scannedCount = 0;

    private Integer activatedCount = 0;

    private Integer skippedNoCurrentClassCount = 0;

    private Integer skippedMultiCurrentClassCount = 0;

    public void addScanned(int count) {
        scannedCount += count;
    }

    public void addActivated() {
        activatedCount++;
    }

    public void addSkippedNoCurrentClass() {
        skippedNoCurrentClassCount++;
    }

    public void addSkippedMultiCurrentClass() {
        skippedMultiCurrentClassCount++;
    }

}
