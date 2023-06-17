package xyz.jiangsier.api.dto;

import xyz.jiangsier.util.TraceUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TraceableDTO {
    @Schema(description = "Trace identifier.")
    private String traceId;

    public TraceableDTO() {
        this.traceId = TraceUtils.getTraceId();
    }
}
