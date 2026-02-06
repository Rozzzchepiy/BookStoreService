package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.component.CartComponent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final BookService bookService;
    private final CartComponent cart;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public String allOrders(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Pageable pageable = createPageable(page, size, sortField, sortDir);

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(
                null, null, search, statuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        prepareOrdersModel(model, pageResult, sortField, sortDir, size, search, statuses, dateFrom, dateTo, minPrice, maxPrice);
        model.addAttribute("pageTitle", "Всі замовлення (Admin)");

        return "orders";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/my")
    public String myClientOrders(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        User client = userRepository.findByEmail(principal.getName()).orElseThrow();

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDTO> ordersPage = orderService.getFilteredOrders(
                client.getId(),
                null,
                search, statuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        addCommonAttributes(model, ordersPage, sortField, sortDir, size, search, statuses, dateFrom, dateTo, minPrice, maxPrice);

        return "orders";
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/my")
    public String myEmployeeOrders(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        User employee = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = createPageable(page, size, sortField, sortDir);

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(
                null, employee.getId(), search, statuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        prepareOrdersModel(model, pageResult, sortField, sortDir, size, search, statuses, dateFrom, dateTo, minPrice, maxPrice);
        model.addAttribute("pageTitle", "Мої замовлення");

        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/available")
    public String availableOrders(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Pageable pageable = createPageable(page, size, sortField, sortDir);
        List<OrderStatus> targetStatuses = List.of(OrderStatus.NEW);

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(
                null, null, search, targetStatuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        prepareOrdersModel(model, pageResult, sortField, sortDir, size, search, targetStatuses, dateFrom, dateTo, minPrice, maxPrice);
        model.addAttribute("pageTitle", "Доступні замовлення");

        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/take")
    public String takeOrder(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        String currentUsername = principal.getName();

        orderService.takeOrder(id, currentUsername);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders/available");
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/refuse")
    public String refuseOrder(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        String currentUsername = principal.getName();

        orderService.updateStatus(id, OrderStatus.NEW , currentUsername);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders/employee/my");
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/canceled")
    public String canceledOrder(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        String currentUsername = principal.getName();

        orderService.refund(id, currentUsername);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders/employee/my");
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/deliver")
    public String deliverOrder(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        String currentUsername = principal.getName();
        orderService.deliver(id, currentUsername);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders/employee/my");
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/client/canceled")
    public String canceledOrderByClient(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        String currentUsername = principal.getName();
        orderService.refund(id, currentUsername);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/orders/client/my");
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{id}")
    public String getAllOrdersByEmployee(
            @PathVariable("id") Long id,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Pageable pageable = createPageable(page, size, sortField, sortDir);

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(
                null, id, search, statuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        prepareOrdersModel(model, pageResult, sortField, sortDir, size, search, statuses, dateFrom, dateTo, minPrice, maxPrice);
        model.addAttribute("pageTitle", "Замовлення працівника #" + id);

        return "orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/client/{id}")
    public String getAllOrderByClient(
            @PathVariable("id") Long id,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Pageable pageable = createPageable(page, size, sortField, sortDir);

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(
                id, null, search, statuses, dateFrom, dateTo, minPrice, maxPrice, pageable
        );

        prepareOrdersModel(model, pageResult, sortField, sortDir, size, search, statuses, dateFrom, dateTo, minPrice, maxPrice);
        model.addAttribute("pageTitle", "Замовлення клієнта #" + id);

        return "orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'CLIENT')")
    @GetMapping("/{id}/details")
    public String getOrderDetails(@PathVariable("id") Long id, Model model,  Principal principal, Authentication authentication) {
        OrderDTO order = orderService.getOrderById(id);
        String currentUsername = principal.getName();
        boolean isAdminOrEmployee = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")
                        || role.getAuthority().equals("ROLE_EMPLOYEE"));

        if (!isAdminOrEmployee && !currentUsername.equals(order.getClientEmail())) {
            throw new AccessDeniedException("error.order.access_denied");
        }

        model.addAttribute("order", order);
        return "order-details";
    }


    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add")
    public String addOrder(@Valid @ModelAttribute("orderDTO") OrderDTO orderDTO, BindingResult result, Principal principal){
        if (result.hasErrors()) {
            return "orders";
        }
        orderService.addOrder(orderDTO, principal.getName());
        return "redirect:/orders/basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/add")
    public String addToBasket(@RequestParam("bookId") Long bookId, @RequestParam(value = "quantity", defaultValue = "1") Integer quantity){
        BookDTO book = bookService.getBookById(bookId);
        cart.addBook(book.getId(), book.getName(), book.getPrice(), quantity);
        return "redirect:/books/" + book.getId();
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/update")
    public String updateBasketQuantity(@RequestParam("bookId") Long bookId,
                                       @RequestParam("quantity") Integer quantity) {
        cart.updateQuantity(bookId, quantity);
        return "redirect:/orders/basket";
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/remove")
    public String removeFromBasket(@RequestParam("bookId") Long bookId) {
        cart.removeItem(bookId);
        return "redirect:/orders/basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/basket")
    public String showBasket(Model model) {
        model.addAttribute("items", cart.getItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/create")
    public String createOrder(Principal principal) {
        if (cart.getItems().isEmpty()) {
            return "redirect:/orders/basket?error=empty";
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBookItems(new ArrayList<>(cart.getItems()));

        try {
            orderService.addOrder(orderDTO, principal.getName());
            cart.clear();
        } catch (RuntimeException e) {
            return "redirect:/orders/basket?error=" + e.getMessage();
        }

        return "redirect:/orders/client/my";
    }

    private void addCommonAttributes(Model model, Page<OrderDTO> page, String sortField, String sortDir, int size,
                                     String search, List<OrderStatus> statuses, LocalDate dateFrom, LocalDate dateTo,
                                     BigDecimal minPrice, BigDecimal maxPrice) {
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("allStatuses", OrderStatus.values());

        model.addAttribute("selectedStatuses", statuses);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("size", size);
    }

    private Pageable createPageable(int page, int size, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        return PageRequest.of(page, size, sort);
    }



    private void prepareOrdersModel(
            Model model,
            Page<OrderDTO> page,
            String sortField,
            String sortDir,
            int size,
            String search,
            List<OrderStatus> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("allStatuses", OrderStatus.values());

        model.addAttribute("search", search);
        model.addAttribute("selectedStatuses", statuses);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("size", size);
    }




}
