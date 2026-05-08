package com.moyz.nexus.common.dto;

import com.moyz.nexus.common.vo.*;
import lombok.Data;

@Data
public class StatisticDto {
    private UserStatistic userStatistic;
    private KbStatistic kbStatistic;
    private TokenCostStatistic tokenCostStatistic;
    private ConvStatistic convStatistic;
    private ImageCostStatistic imageCostStatistic;
}
