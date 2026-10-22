@Setter
@Getter
@NoArgsConstructor
public class EmployeeTransferEventDTO{
private Long employeeId;
private Long fromDepartmentId;
private Long toDepartmentId;
private String reason;
private LocalDateTime transferredAt;
}
