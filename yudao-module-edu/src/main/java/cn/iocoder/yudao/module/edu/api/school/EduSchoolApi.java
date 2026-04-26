package cn.iocoder.yudao.module.edu.api.school;

import cn.iocoder.yudao.module.edu.api.school.dto.EduSchoolSimpleRespDTO;

import java.util.List;

public interface EduSchoolApi {

    List<EduSchoolSimpleRespDTO> getSchoolSimpleList();

}
