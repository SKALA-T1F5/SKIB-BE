package com.t1f5.skib.global.customAnnotations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation()
@ApiResponses(
    value =
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류가 발생했습니다..",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(value = "{\"message\":\"서버 오류가 발생했습니다.\",\"status\":500}"))))
public @interface SwaggerInternetServerError {}
