package com.moyz.nexus.common.validator;

import com.moyz.nexus.common.annotation.CreateImageReqCheck;
import com.moyz.nexus.common.dto.CreateImageDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import static com.moyz.nexus.common.cosntant.NexusConstant.GenerateImage.*;

public class CreateImageReqValidator implements
        ConstraintValidator<CreateImageReqCheck, CreateImageDto> {
    @Override
    public boolean isValid(CreateImageDto createImageDto, ConstraintValidatorContext constraintValidatorContext) {
        if (createImageDto.getInteractingMethod() == INTERACTING_METHOD_GENERATE_IMAGE && StringUtils.isBlank(createImageDto.getPrompt())) {
            throw new IllegalArgumentException("Prompt can not be empty");
        }
        return true;
    }
}
