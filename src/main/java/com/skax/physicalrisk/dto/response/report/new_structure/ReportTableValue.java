package com.skax.physicalrisk.dto.response.report.new_structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTableValue {
    private String value;

    @JsonProperty("bg_color")
    private String bgColor;
}
