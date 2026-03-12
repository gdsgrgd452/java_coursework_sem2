package com.example.safedispatch.controller;

import com.example.safedispatch.error.ErrorInfo;
import com.example.safedispatch.model.Assignment;
import com.example.safedispatch.model.Department;
import com.example.safedispatch.model.Employee;
import com.example.safedispatch.repository.AssignmentRepository;
import com.example.safedispatch.repository.DepartmentRepository;
import com.example.safedispatch.repository.EmployeeRepository;
import com.example.safedispatch.service.AssignmentService;
import com.example.safedispatch.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {


    private final EmployeeRepository employeeRepo;
    private final AssignmentRepository assignmentRepo;
    private final DepartmentRepository departmentRepo;
    private final AssignmentService assignmentService;
    private final EmployeeService employeeService;

    @Autowired
    public MainController(EmployeeRepository employeeRepo, AssignmentRepository assignmentRepo, DepartmentRepository departmentRepo, AssignmentService assignmentService, EmployeeService employeeService) {
         this.employeeRepo = employeeRepo;
         this.assignmentRepo = assignmentRepo;
         this.departmentRepo = departmentRepo;
         this.assignmentService = assignmentService;
         this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getEmployees() {
        List<Employee> employees = employeeRepo.findAll();
        if (employees.isEmpty()) {
            return new ResponseEntity<>(new ErrorInfo("No employees found."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Integer id) {
        Employee employee = employeeRepo.findById(id).orElse(null);
        if (employee == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee with id: " + id + " not found."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee, UriComponentsBuilder ucBuilder) {
        employeeRepo.save(employee);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/api/employees/{id}").buildAndExpand(employee.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        Employee employeeInDb = employeeRepo.findById(id).orElse(null);
        if (employeeInDb == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee with id: " + id + " not found."), HttpStatus.NOT_FOUND);
        }

        employeeInDb.setName(employee.getName());
        employeeInDb.setContractType(employee.getContractType());
        employeeInDb.setStartDate(employee.getStartDate());
        employeeInDb.setSalary(employee.getSalary());
        employeeInDb.setEmail(employee.getEmail());

        Employee updatedEmployee = employeeRepo.save(employeeInDb);

        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer id) {
        Employee employee = employeeRepo.findById(id).orElse(null);
        if (employee == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee with id " + id + " not found."), HttpStatus.NOT_FOUND);
        }
        employeeRepo.deleteById(id);
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments() {
        List<Department> departments = departmentRepo.findAll();
        if (departments.isEmpty()) {
            return new ResponseEntity<>(new ErrorInfo("No departments found."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(departments, HttpStatus.OK);
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        if (department.getBudget() <= 0) {
            return new ResponseEntity<>(new ErrorInfo("Budget must be positive."), HttpStatus.BAD_REQUEST);
        }
        Department savedDept = departmentRepo.save(department);
        return new ResponseEntity<>(savedDept, HttpStatus.CREATED);
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> assignEmployee(@RequestBody Map<String, String> request) {
        try {
            Integer.parseInt(request.get("employeeId"));
            Integer.parseInt(request.get("departmentId"));
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(new ErrorInfo("Invalid input format. EmployeeId and DepartmentId must be integers."), HttpStatus.BAD_REQUEST);
        }
        Integer employeeId = Integer.valueOf(request.get("employeeId"));
        Integer departmentId = Integer.valueOf(request.get("departmentId"));
        String role = request.get("role");
        String accessLevel = request.get("accessLevel");

        Employee employee = employeeRepo.findById(employeeId).orElse(null);
        Department department = departmentRepo.findById(departmentId).orElse(null);

        if (employee == null && department == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee and Department both not found - employee id: " + employeeId + " and department id: " + departmentId + " ."), HttpStatus.NOT_FOUND);
        }
        if (employee == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee not found with id: " + employeeId + " ."), HttpStatus.NOT_FOUND);
        }
        if (department == null) {
            return new ResponseEntity<>(new ErrorInfo("Department not found with id: " + departmentId + " ."), HttpStatus.NOT_FOUND);
        }

        Assignment assignment = assignmentService.assign(employee, department, role, accessLevel);
        return new ResponseEntity<>(assignment, HttpStatus.OK);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable int id) {
        Assignment assignment = assignmentRepo.findById(id).orElse(null);
        if (assignment == null) {
            return new ResponseEntity<>(new ErrorInfo("Assignment with id " + id + " not found."), HttpStatus.NOT_FOUND);
        }
        assignmentRepo.delete(assignment);
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @PutMapping("/employees/{id}/promote")
    public ResponseEntity<?> promoteEmployee(@PathVariable int id) {
        Employee employee = employeeRepo.findById(id).orElse(null);
        if (employee == null) {
            return new ResponseEntity<>(new ErrorInfo("Employee with id " + id + " not found."), HttpStatus.NOT_FOUND);
        }

        if (employee.getStartDate().isAfter(LocalDate.now().minusMonths(6))) {
            return new ResponseEntity<>(new ErrorInfo("Employee is not eligible for promotion (less than 6 months)."), HttpStatus.BAD_REQUEST);
        }

        Employee promotedEmployee = employeeService.promote(employee);
        return new ResponseEntity<>(promotedEmployee, HttpStatus.OK);
    }

}
