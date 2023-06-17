package xyz.jiangsier.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "User basic information.")
@Data
public class UserBasicInfoDTO extends TraceableDTO{
    @Schema(description = "Username")
    private String username;
    @Schema(description = "Nickname")
    private String nickname;
    @Schema(description = "Picture/Atavar")
    private String picture;
}
