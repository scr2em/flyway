package com.Flyway.Flyway.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToInvitationRequest {
    @NotBlank(message = "Response is required")
    @Pattern(regexp = "^(accept|reject)$", message = "Response must be 'accept' or 'reject'")
    private String response;
}

