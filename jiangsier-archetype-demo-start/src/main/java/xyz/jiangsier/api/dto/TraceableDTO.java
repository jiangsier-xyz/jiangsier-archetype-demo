package xyz.jiangsier.api.dto;

import xyz.jiangsier.util.TraceUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
@Data
@JsonInclude(NON_NULL)
public class TraceableDTO {
    @Schema(description = "Trace identifier.")
    private String requestId;

    public TraceableDTO() {
        this.requestId = TraceUtils.getTraceId();
    }
}
