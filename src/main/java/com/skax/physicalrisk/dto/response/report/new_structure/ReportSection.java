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
public class ReportSection {
    @JsonProperty("section_id")
    private String sectionId;
    private String title;
    private List<ReportBlock> blocks;
}
