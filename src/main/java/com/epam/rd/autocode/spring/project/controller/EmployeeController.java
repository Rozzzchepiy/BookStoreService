package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.EmployeeSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String getAllEmployees(Model model, EmployeeSearchRequest request) {
        Page<EmployeeDTO> employeesPage = employeeService.getAllEmployees(request);

        model.addAttribute("employees", employeesPage);
        model.addAttribute("filter", request);
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
                                 BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "employee_form";
        }

        try {
            employeeService.addEmployee(employeeDTO);
        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("email", "validation.email.exists");
            return "employee_form";
        }

        return "redirect:/employees";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public String getEmployeeById(@PathVariable("id") Long id,  Model model){
        EmployeeDTO employee = employeeService.getEmployeeById(id);
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
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 @PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "employee_form";
        }
        try {
            employeeService.updateEmployee(id, employeeDTO);
            redirectAttributes.addAttribute("msg", "employee.update.success");
        } catch (Exception e) {
            bindingResult.rejectValue("email", "validation.email.exists");
            return "employee_form";
        }
        return "redirect:/employees";
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id,
                                 EmployeeSearchRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addAttribute("msg", "employee.delete.success");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addAttribute("error", "error.employee.delete_constraint");
        }

        redirectAttributes.addAttribute("page", request.getPage());
        if (request.getKeyword() != null) {
            redirectAttributes.addAttribute("keyword", request.getKeyword());
        }

        return "redirect:/employees";
    }



}
