package com.skax.physicalrisk.dto.response.report.new_structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    @JsonProperty("report_id")
    private String reportId;
    private ReportMeta meta;
    private List<ReportSection> sections;
}
