package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.nexus.common.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("nexus_user")
@Schema(title = "User对象")
public class User extends BaseEntity {

    @Schema(name = "用户名称")
    @TableField("name")
    private String name;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField("uuid")
    private String uuid;

    @Schema(name = "上下文理解中需要携带的消息对数量（提示词及回复�?)
    @TableField("understand_context_msg_pair_num")
    private Integer understandContextMsgPairNum;

    @Schema(name = "token quota in one day")
    @TableField("quota_by_token_daily")
    private Integer quotaByTokenDaily;

    @Schema(name = "token quota in one month")
    @TableField("quota_by_token_monthly")
    private Integer quotaByTokenMonthly;

    @Schema(name = "request quota in one day")
    @TableField("quota_by_request_daily")
    private Integer quotaByRequestDaily;

    @Schema(name = "request quota in one month")
    @TableField("quota_by_request_monthly")
    private Integer quotaByRequestMonthly;

    @TableField("quota_by_image_daily")
    private Integer quotaByImageDaily;

    @TableField("quota_by_image_monthly")
    private Integer quotaByImageMonthly;

    @TableField("user_status")
    private UserStatusEnum userStatus;

    @TableField("active_time")
    private LocalDateTime activeTime;

    @Schema(title = "是否管理员（0：否�?：是�?)
    @TableField(value = "is_admin")
    private Boolean isAdmin;
}
