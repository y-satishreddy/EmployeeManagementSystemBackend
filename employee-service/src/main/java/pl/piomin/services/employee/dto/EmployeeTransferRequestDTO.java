package pl.piomin.services.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeTransferRequestDTO {
  @NotNull(message = "Target Department Id is required")
  @Positive(message = "Target Department Id must be greater than 0")
  private Long targetDepartmentId;
  @NotBlank(message = "Reason is required")
  private String reason;
}
