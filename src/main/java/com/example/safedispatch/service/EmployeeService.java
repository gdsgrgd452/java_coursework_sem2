package com.example.safedispatch.service;

import com.example.safedispatch.model.Employee;
import com.example.safedispatch.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    public Employee promote(Employee employee) {

        employee.setSalary(employee.getSalary() * 1.1f);
        employee.setContractType("Permanent"); //I dont think this is correct but not sure what the instructions mean

        return employeeRepo.save(employee);
    }
}
