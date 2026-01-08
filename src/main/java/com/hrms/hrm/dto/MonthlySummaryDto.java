package com.hrms.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlySummaryDto {
    private int present;
    private int halfDay;
    private int leave;
    private int weekend;
    private int payableDays;

    public void add(MonthlySummaryDto other) {
        this.present += other.getPresent();
        this.halfDay += other.getHalfDay();
        this.leave += other.getLeave();
        this.weekend += other.getWeekend();
        this.payableDays += other.getPayableDays();
    }

}
