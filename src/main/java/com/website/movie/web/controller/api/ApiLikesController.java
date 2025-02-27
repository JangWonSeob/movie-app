package com.website.movie.web.controller.api;

import com.website.movie.biz.dto.LikesDto;
import com.website.movie.biz.dto.UserDto;
import com.website.movie.biz.model.JsonResult;
import com.website.movie.biz.service.LikesService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class ApiLikesController {

    private final LikesService likesService;

    @PostMapping("/api/likes/set.api")
    @ApiOperation(value = "좋아요 등록 및 삭제 API", notes = "좋아요 등록 및 삭제가 가능합니다.")
    public JsonResult set(@AuthenticationPrincipal UserDto user, @RequestBody LikesDto parameter) {

        if (user == null) {
            return JsonResult.fail("접근 권한이 없습니다.");
        }
        parameter.setLoginUserId(user.getId());

        boolean likesYn = likesService.set(parameter);

        HashMap<String, Boolean> result = new HashMap<>();
        result.put("likesYn", likesYn);

        return JsonResult.success(result);
    }
}
