package com.capital.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateMerchantRequest {
    
    @NotBlank(message = "Merchant name must not blank")
    @Size(min = 2, max = 100, message = "Merchant name's length must between 2-100")
    @Schema(description = "Merchant name")
    private String name;
    
    @NotBlank(message = "Merchant code must not blank")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "The merchant code can only contain letters, numbers, underscores, and dashes")
    @Size(min = 3, max = 50, message = "Merchant code's length must between 3-50")
    @Schema(description = "Merchant code")
    private String code;
}
