package com.example.safedispatch.controller;

import com.example.safedispatch.exception.BusinessRuleException;
import com.example.safedispatch.exception.ResourceNotFoundException;
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
    public ResponseEntity<List<Employee>> getEmployees() {
        List<Employee> employees = employeeRepo.findAll();
        if (employees.isEmpty()) {
            throw new ResourceNotFoundException("No employees found.");
        }
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Integer id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id: " + id + " not found."));
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/employees")
    public ResponseEntity<Void> createEmployee(@RequestBody Employee employee, UriComponentsBuilder ucBuilder) {
        employeeRepo.save(employee);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/api/employees/{id}").buildAndExpand(employee.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        Employee employeeInDb = employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id: " + id + " not found."));

        employeeInDb.setName(employee.getName());
        employeeInDb.setContractType(employee.getContractType());
        employeeInDb.setStartDate(employee.getStartDate());
        employeeInDb.setSalary(employee.getSalary());
        employeeInDb.setEmail(employee.getEmail());

        Employee updatedEmployee = employeeRepo.save(employeeInDb);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Integer> deleteEmployee(@PathVariable Integer id) {
        if (!employeeRepo.existsById(id)) {
            throw new ResourceNotFoundException("Employee with id " + id + " not found.");
        }
        employeeRepo.deleteById(id);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepo.findAll();
        if (departments.isEmpty()) {
            throw new ResourceNotFoundException("No departments found.");
        }
        return ResponseEntity.ok(departments);
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        if (department.getBudget() <= 0) {
            throw new BusinessRuleException("Budget must be positive.");
        }
        Department savedDept = departmentRepo.save(department);
        return new ResponseEntity<>(savedDept, HttpStatus.CREATED);
    }

    @PostMapping("/assignments")
    public ResponseEntity<Assignment> assignEmployee(@RequestBody Map<String, String> request) {
        int employeeId;
        int departmentId;
        try {
            employeeId = Integer.parseInt(request.get("employeeId"));
            departmentId = Integer.parseInt(request.get("departmentId"));
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("Invalid input format. EmployeeId and DepartmentId must be integers.");
        }

        String role = request.get("role");
        String accessLevel = request.get("accessLevel");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        Assignment assignment = assignmentService.assign(employee, department, role, accessLevel);
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Integer> deleteAssignment(@PathVariable int id) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment with id " + id + " not found."));

        assignmentRepo.delete(assignment);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/employees/{id}/promote")
    public ResponseEntity<Employee> promoteEmployee(@PathVariable int id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found."));

        if (employee.getStartDate().isAfter(LocalDate.now().minusMonths(6))) {
            throw new BusinessRuleException("Employee is not eligible for promotion (less than 6 months).");
        }

        Employee promotedEmployee = employeeService.promote(employee);
        return ResponseEntity.ok(promotedEmployee);
    }
}