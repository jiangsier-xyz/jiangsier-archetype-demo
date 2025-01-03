package xyz.jiangsier.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.jiangsier.api.dto.UserBasicInfoDTO;
import xyz.jiangsier.api.dto.UserDetailsDTO;
import xyz.jiangsier.api.util.AuthUtils;
import xyz.jiangsier.api.util.CommonUtils;
import xyz.jiangsier.model.User;
import xyz.jiangsier.service.account.SysUserService;

@Controller
@Tag(name = "account")
@RequestMapping("/api/account")
@ResponseBody
@Validated
@SuppressWarnings("unused")
public class AccountApi {
    @Autowired
    private SysUserService userService;

    @Operation(
            summary = "Get basic user information.",
            description = "Return the basic user information of the account, including: username, nickname, picture/avatar link."
    )
    @GetMapping("/basic/{username}")
    public UserBasicInfoDTO basic(
            @Schema(description = "Username") @PathVariable("username") @NotBlank String username) {
        User user = userService.loadUserByUsername(username);
        return CommonUtils.convert(user, UserBasicInfoDTO.class);
    }

    @Operation(
            summary = "Get current user details.",
            description = "Return the current user details of the account, fields refer to OpenID Connect (OIDC) [standard claims](https://openid.net/specs/openid-connect-core-1_0.html#Claims)。")
    @GetMapping("/details")
    public UserDetailsDTO details() {
        User user = AuthUtils.currentUser();
        return CommonUtils.convert(user, UserDetailsDTO.class);
    }
}
