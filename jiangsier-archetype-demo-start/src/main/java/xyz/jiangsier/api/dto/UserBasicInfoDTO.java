package xyz.jiangsier.api.dto;

import xyz.jiangsier.api.util.CommonUtils;
import xyz.jiangsier.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

@Schema(description = "User basic information.")
@Data
public class UserBasicInfoDTO extends TraceableDTO{
    @Schema(description = "Username")
    private String username;
    @Schema(description = "Nickname")
    private String nickname;
    @Schema(description = "Picture/Atavar")
    private String picture;

    public static UserBasicInfoDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        return CommonUtils.convert(user, UserBasicInfoDTO.class);
    }
}
