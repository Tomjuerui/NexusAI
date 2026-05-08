package com.moyz.nexus.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户的统计信�?
 */
@Data
public class UserStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer todayCreated;
    private Integer todayActivated;
    private Integer totalNormal;
}
