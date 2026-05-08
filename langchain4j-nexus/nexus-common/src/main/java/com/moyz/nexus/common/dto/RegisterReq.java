package com.moyz.nexus.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Schema(name = "注册请求参数")
@Data
@Validated
public class RegisterReq {

    @Parameter(description = "邮箱")
    @Email
    private String email;

    @Parameter(description = "密码")
    @Min(6)
    private String password;

    @Parameter(description = "验证码ID")
    @Length(min = 32)
    private String captchaId;

    @Parameter(description = "验证�?)
    @Length(min = 4, max = 4)
    private String captchaCode;
}
