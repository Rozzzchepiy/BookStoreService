package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public String getAllEmployees(Model model){
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        employees.forEach(e -> e.setPassword(null));
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees";
    }

    @GetMapping("/{id}")
    public String getEmployeeById(@PathVariable("id") Long id,  Model model){
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        employee.setPassword(null);
        model.addAttribute("employee", employee);
        return "employee";
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@Valid @ModelAttribute EmployeeDTO employeeDTO, BindingResult bindingResult, @PathVariable("id") Long id, Model model){
        if(bindingResult.hasErrors()){
            return "employee";
        }
        employeeService.updateEmployee(id, employeeDTO);
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        return "redirect:/employees/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id){
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }

}
