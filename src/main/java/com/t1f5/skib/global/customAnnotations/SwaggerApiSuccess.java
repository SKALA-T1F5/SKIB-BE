package com.t1f5.skib.global.customAnnotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Operation()
@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "200 성공 예시", value = "")))
public @interface SwaggerApiSuccess {
        String summary() default "";

        String description();

        String value();
}

