package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String getAllEmployees(Model model){
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        employees.forEach(e -> e.setPassword(null));
        model.addAttribute("employees", employees);
        return "employees";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String createEmployeeForm(Model model) {
        model.addAttribute("employee", new EmployeeDTO());
        return "employee_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String createEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            return "employee_form";
        }

        try {
            employeeService.addEmployee(employeeDTO);
        } catch (Exception e) {
            bindingResult.rejectValue("email", "validation.email.exists");
            return "employee_form";
        }

        return "redirect:/employees";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public String getEmployeeById(@PathVariable("id") Long id,  Model model){
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        employee.setPassword(null);
        model.addAttribute("employee", employee);
        return "employee";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable("id") Long id, Model model){
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        return "employee_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO, BindingResult bindingResult, @PathVariable("id") Long id) {
        if (bindingResult.hasErrors()) {
            return "employee_form";
        }
        try {
        employeeService.updateEmployee(id, employeeDTO);
        }catch(Exception e){
            bindingResult.rejectValue("email", "validation.email.exists");
            return "employee_form";}
        return  "redirect:/employees";
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "error.employee.delete_constraint");
        }
        return "redirect:/employees";
    }

}
