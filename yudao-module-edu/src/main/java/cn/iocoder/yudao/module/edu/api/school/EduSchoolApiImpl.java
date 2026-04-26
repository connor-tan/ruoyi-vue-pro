package cn.iocoder.yudao.module.edu.api.school;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.school.dto.EduSchoolSimpleRespDTO;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class EduSchoolApiImpl implements EduSchoolApi {

    @Resource
    private SchoolService schoolService;

    @Override
    public List<EduSchoolSimpleRespDTO> getSchoolSimpleList() {
        return BeanUtils.toBean(schoolService.getSchoolSimpleList(), EduSchoolSimpleRespDTO.class);
    }
}
