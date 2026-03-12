package com.example.safedispatch.service;

import com.example.safedispatch.model.Assignment;
import com.example.safedispatch.model.Department;
import com.example.safedispatch.model.Employee;
import com.example.safedispatch.repository.AssignmentRepository;
import com.example.safedispatch.repository.DepartmentRepository;
import com.example.safedispatch.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentService {

    private final EmployeeRepository employeeRepo;
    private final AssignmentRepository assignmentRepo;
    private final DepartmentRepository departmentRepo;

    @Autowired
    public AssignmentService(EmployeeRepository employeeRepo, AssignmentRepository assignmentRepo, DepartmentRepository departmentRepo) {
        this.employeeRepo = employeeRepo;
        this.assignmentRepo = assignmentRepo;
        this.departmentRepo = departmentRepo;
    }

    public Assignment assign(Employee employee, Department department, String role, String accessLevel) {
        Assignment assignment = new Assignment();
        assignment.setRole(role);
        assignment.setAccessLevel(accessLevel);

        //Employee's assignments
        List<Assignment> currentEmployeesAssignments = employee.getAssignments();
        currentEmployeesAssignments.add(assignment);
        employee.setAssignments(currentEmployeesAssignments);

        //Assignment's employees'
        List<Employee> currentEmployees = new ArrayList<>();
        currentEmployees.add(employee);
        assignment.setEmployee(currentEmployees);

        //Department's assignments'
        List<Assignment> currentDepartmentsAssignments = department.getAssignments();
        currentDepartmentsAssignments.add(assignment);
        department.setAssignments(currentDepartmentsAssignments);

        //Assignment's department
        assignment.setDepartment(department);

        departmentRepo.save(department);
        employeeRepo.save(employee);
        return assignmentRepo.save(assignment);
    }

}
