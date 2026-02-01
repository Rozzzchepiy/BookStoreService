package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final BookService bookService;
    private final CartController cartController;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public String allOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/my")
    public String myClientOrders(Model model, Principal principal) {
        model.addAttribute(
                "orders",
                orderService.getOrdersByClientEmail(principal.getName())
        );
        return "orders";
    }



    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/my")
    public String myEmployeeOrders(Model model, Principal principal) {
        model.addAttribute(
                "orders",
                orderService.getOrdersByEmployeeEmail(principal.getName())
        );
        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/available")
    public String availableOrders(Model model) {
        model.addAttribute(
                "orders",
                orderService.getOrdersByStatus(OrderStatus.NEW)
        );
        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/take")
    public String takeOrder(@PathVariable Long id, Model model, Principal principal) {
        String currentUsername = principal.getName();

        orderService.takeOrder(id, currentUsername);

        return "redirect:/orders/available";
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/refuse")
    public String refuseOrder(@PathVariable Long id, Model model, Principal principal) {
        String currentUsername = principal.getName();

        orderService.updateStatus(id, OrderStatus.NEW , currentUsername);

        return "redirect:/orders/employee/my";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/canceled")
    public String canceledOrder(@PathVariable Long id, Principal principal) {
        String currentUsername = principal.getName();

        orderService.refund(id, currentUsername);

        return "redirect:/orders/employee/my";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/client/canceled")
    public String canceledOrderByClient(@PathVariable Long id, Principal principal) {
        String currentUsername = principal.getName();
        orderService.refund(id, currentUsername);
        return "redirect:/orders/client/my";
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{id}")
    public String getAllOrdersByEmployee(@PathVariable("id") Long id, Model model){
        List<OrderDTO> orders = orderService.getOrdersByEmployee(id);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/client/{id}")
    public String getAllOrderByClient(@PathVariable("id") Long id, Model model){
        List<OrderDTO> orders = orderService.getOrdersByClient(id);
        model.addAttribute("orders", orders);
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
            throw new AccessDeniedException("Ви не маєте права переглядати це замовлення");
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
        cartController.addBook(book.getId(), book.getName(), book.getPrice(), quantity);
        return "redirect:/books/" + book.getId();
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/update")
    public String updateBasketQuantity(@RequestParam("bookId") Long bookId,
                                       @RequestParam("quantity") Integer quantity) {
        cartController.updateQuantity(bookId, quantity);
        return "redirect:/orders/basket";
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/remove")
    public String removeFromBasket(@RequestParam("bookId") Long bookId) {
        cartController.removeItem(bookId);
        return "redirect:/orders/basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/basket")
    public String showBasket(Model model) {
        model.addAttribute("items", cartController.getItems());
        model.addAttribute("totalPrice", cartController.getTotalPrice());
        return "basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/create")
    public String createOrder(Principal principal) {
        if (cartController.getItems().isEmpty()) {
            return "redirect:/orders/basket?error=empty";
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBookItems(new ArrayList<>(cartController.getItems()));

        try {
            orderService.addOrder(orderDTO, principal.getName());
            cartController.clear();
        } catch (RuntimeException e) {
            return "redirect:/orders/basket?error=" + e.getMessage();
        }

        return "redirect:/orders/client/my";
    }


}
