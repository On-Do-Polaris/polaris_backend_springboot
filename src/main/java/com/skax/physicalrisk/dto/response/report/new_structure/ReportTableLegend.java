package com.skax.physicalrisk.dto.response.report.new_structure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTableLegend {
    private String color;
    private String label;
}
