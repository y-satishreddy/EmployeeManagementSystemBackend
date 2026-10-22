public class DepartmentTransferService{

  @Transactional
public void processEmployeeTransfer(EmployeeTransferEventDTO event) {

    Department oldDepartment = departmentRepository
            .findById(event.getFromDepartmentId())
            .orElseThrow(() -> new DepartmentNotFoundException(
                    "Old department not found: "
                            + event.getFromDepartmentId()));

    Department newDepartment = departmentRepository
            .findById(event.getToDepartmentId())
            .orElseThrow(() -> new DepartmentNotFoundException(
                    "Target department not found: "
                            + event.getToDepartmentId()));

    oldDepartment.setHeadcount(oldDepartment.getHeadcount() - 1);

    newDepartment.setHeadcount(newDepartment.getHeadcount() + 1);

    departmentRepository.save(oldDepartment);
    departmentRepository.save(newDepartment);

    log.info(
            "Employee transfer processed: employeeId={}, fromDepartmentId={}, toDepartmentId={}",
            event.getEmployeeId(),
            event.getFromDepartmentId(),
            event.getToDepartmentId()
    );
}
}
