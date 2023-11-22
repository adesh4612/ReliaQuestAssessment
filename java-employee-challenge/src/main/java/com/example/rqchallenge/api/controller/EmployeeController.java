package com.example.rqchallenge.api.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.rqchallenge.api.model.Employee;
import com.example.rqchallenge.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmployeeController implements IEmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {

        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() throws IOException {
        List<Employee> result = this.employeeService.getAllEmployees();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (result != null)
            status = HttpStatus.OK;
        return new ResponseEntity<>(result, status);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> result = this.employeeService.getEmployeesByMatchingName(searchString);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        Employee result = this.employeeService.getEmployeeById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        Integer result = this.employeeService.getHighestSalaryOfEmployees();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> result = this.employeeService.getTopTenHighestEarningEmployeeNames();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(Map<String, Object> employeeInput) {;
        Employee result = this.employeeService.createEmployee(employeeInput);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        String result = this.employeeService.deleteEmployeeById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
