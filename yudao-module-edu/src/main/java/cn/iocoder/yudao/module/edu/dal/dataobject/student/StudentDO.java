package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 学生 DO
 *
 * @author connor
 */
@TableName("edu_student")
@KeySequence("edu_student_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDO extends BaseDO {

    /**
     * 学生ID
     */
    @TableId
    private Long id;
    /**
     * 姓名
     */
    private String studentName;
    /**
     * 家长
     */
    private Long belongTo;
    /**
     * 学校
     */
    private Long currentSchoolId;
    /**
     * 入学年
     */
    private Integer entryYear;
    /**
     * 学号
     */
    private Integer studentCode;
    /**
     * 状态
     */
    private Integer status;


}
