public class EmployeeTransferListener{

  @KafkaListener(topics="employee-transferred")
  public void consumeTransferEvent(EmployeeTransferEventDTO event){
   log.info(
            "Received employee transfer event: employeeId={}, fromDepartmentId={}, toDepartmentId={}, reason={}",
            event.getEmployeeId(),
            event.getFromDepartmentId(),
            event.getToDepartmentId(),
            event.getReason()
        );
    departmentService.processEmployeeTransfer(event);
  }
}
