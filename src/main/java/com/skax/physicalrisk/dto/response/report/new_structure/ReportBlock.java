package com.skax.physicalrisk.dto.response.report.new_structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportBlock {
    private String type; // "text" or "table"
    private String subheading;

    // For "text" type
    private String content;

    // For "table" type
    private String title;
    private List<ReportTableHeader> headers;
    private List<Map<String, Object>> items;
    private List<ReportTableLegend> legend;
}
