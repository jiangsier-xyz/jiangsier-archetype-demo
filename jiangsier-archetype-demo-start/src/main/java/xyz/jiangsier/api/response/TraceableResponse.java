package xyz.jiangsier.api.response;

import xyz.jiangsier.util.TraceUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Traceable response.")
@Data
public class TraceableResponse {
    @Schema(description = "Trace identifier.")
    private String traceId;

    public TraceableResponse() {
        this.traceId = TraceUtils.getTraceId();
    }
}
