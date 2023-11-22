package com.example.rqchallenge.service;

import java.util.*;
import java.util.stream.Collectors;

import com.example.rqchallenge.api.model.Employee;
import org.json.JSONArray;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

class EmployeeRestService {
    abstract class RequestService {
        abstract Object processRequest();

        public Object serveRequest() {
            try {
                return this.processRequest();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    System.err.println("Client error: " + e.getStatusCode() + " - " + e.getStatusText());
                    System.err.println("Response Body: " + e.getResponseBodyAsString());
                } else {
                    System.err.println("Unexpected HTTP status: " + e.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
            return null;
        }

    }

    public List<Employee> fetchEmployeeData() {
        EmployeeRestService.RequestService requestService = new EmployeeRestService.RequestService() {
            @Override
            Object processRequest() {
                RestTemplate restTemplate = new RestTemplate();
                String apiUrl = Endpoint.V1_ENDPOINT + "/employees";
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    List<Employee> result = new ArrayList<Employee>();
                    String response_body = responseEntity.getBody();
                    JSONObject root = new JSONObject(response_body);
                    JSONArray employees = root.getJSONArray("data");
                    for (int i = 0; i < employees.length(); i++) {
                        JSONObject employeeJson = employees.getJSONObject(i);
                        result.add(new EmployeeService.JSONObjtoEmployeeMapper().mapJSONToEmployee(employeeJson));
                    }
                    return result;
                } else {
                    System.err.println("Unexpected HTTP status: " + responseEntity.getStatusCode());
                }
                return null;
            }
        };
        return (List<Employee>) requestService.serveRequest();
    }

    public Employee getEmployeeById(String Id) {
        RequestService requestService = new RequestService() {
            @Override
            Object processRequest() {
                RestTemplate restTemplate = new RestTemplate();
                String apiUrl = Endpoint.V1_ENDPOINT + "/employee/" + Id;
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String response_body = responseEntity.getBody();
                    JSONObject root = new JSONObject(response_body);
                    JSONObject employeeJson = root.getJSONObject("data");
                    if (employeeJson != null)
                        return new EmployeeService.JSONObjtoEmployeeMapper().mapJSONToEmployee(employeeJson);
                } else {
                    System.err.println("Unexpected HTTP status: " + responseEntity.getStatusCode());
                }
                return null;
            }
        };
        return (Employee) requestService.serveRequest();
    }

    public Employee createEmployee(Map<String, Object> employeeInput) {

        RequestService requestService = new RequestService() {
            @Override
            Object processRequest() {
                RestTemplate restTemplate = new RestTemplate();
                String apiUrl = Endpoint.V1_ENDPOINT + "/create";
                Employee e = new Employee();
                e.setEmployee_name((String) employeeInput.get("employee_name"));
                e.setEmployee_salary((Integer) employeeInput.get("employee_salary"));
                e.setEmployee_age((Integer) employeeInput.get("employee_age"));
                System.out.println("employeee to create " + e);
                HttpEntity<Employee> request = new HttpEntity<>(e);
                Employee createdEmployee = restTemplate.postForObject(apiUrl, request, Employee.class);
                return createdEmployee;
            }
        };
        return (Employee) requestService.serveRequest();
    }

    public String deleteEmployeeById(String Id) {

        RequestService requestService = new RequestService() {
            @Override
            Object processRequest() {
                RestTemplate restTemplate = new RestTemplate();
                String apiUrl = Endpoint.V1_ENDPOINT + "/delete/" + Id;
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("id", Id);
                restTemplate.delete(apiUrl, paramsMap);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "success");
                jsonObject.put("message", "successfully! deleted Record");
                return jsonObject.toString();
            }

        };
        return (String) requestService.serveRequest();
    }

}

@Service
public class EmployeeService {

    static class JSONObjtoEmployeeMapper {
        public Employee mapJSONToEmployee(JSONObject employeeJson) {
            Integer id = employeeJson.getInt("id");
            String employee_name = employeeJson.getString("employee_name");
            Integer employee_salary = employeeJson.getInt("employee_salary");
            Integer employee_age = employeeJson.getInt("employee_age");
            String profile_image = employeeJson.getString("profile_image");
            return new Employee(id, employee_name, employee_salary, employee_age, profile_image);
        }

    }

    public List<Employee> getAllEmployees() {
        return new EmployeeRestService().fetchEmployeeData();
    }

    public List<Employee> getEmployeesByMatchingName(String searchString) {
        List<Employee> employees = new EmployeeRestService().fetchEmployeeData();
        List<Employee> result = employees.parallelStream().filter(e -> e.getEmployee_name().toLowerCase().contains(searchString.toLowerCase())).collect(Collectors.toList());
        return result;
    }

    public Employee getEmployeeById(String Id) {
        return new EmployeeRestService().getEmployeeById(Id);
    }

    public Integer getHighestSalaryOfEmployees() {
        List<Employee> employees = new EmployeeRestService().fetchEmployeeData();
        if (employees == null) {
            return 0;
        }
        OptionalInt maxSalary = employees.stream().mapToInt(Employee::getEmployee_salary).max();
        return maxSalary.orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = new EmployeeRestService().fetchEmployeeData();
        if (employees == null) {
            return new ArrayList<>();
        }
        List<String> employeeNamesWithHighestSalary = employees.stream().sorted(Comparator.comparingInt(Employee::getEmployee_salary).reversed()).limit(10).map(Employee::getEmployee_name).collect(Collectors.toList());
        return employeeNamesWithHighestSalary;
    }

    public Employee createEmployee(Map<String, Object> employeeInput) {
        return new EmployeeRestService().createEmployee(employeeInput);
    }

    public String deleteEmployeeById(String Id) {
        EmployeeRestService employeeRestService = new EmployeeRestService();
        Employee e = employeeRestService.getEmployeeById(Id);
        if (e == null){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "Error! Employee Record may have been already deleted");
            return jsonObject.toString();
        }
        return new EmployeeRestService().deleteEmployeeById(Id);
    }

}

