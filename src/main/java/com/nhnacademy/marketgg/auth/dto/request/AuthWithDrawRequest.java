package com.nhnacademy.marketgg.auth.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AuthWithDrawRequest {

    private LocalDateTime deletedAt;

}
