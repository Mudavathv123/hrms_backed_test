package com.hrms.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryDto {

    private int present;
    private int halfDay;
    private int leave;
    private int weekend;
    private int payableDays;
}
