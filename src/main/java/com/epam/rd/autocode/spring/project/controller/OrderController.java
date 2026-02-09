package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotEnoughMoneyException;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public String allOrders(Model model, OrderSearchRequest request) {
        Page<OrderDTO> pageResult = orderService.getFilteredOrders(request);
        populateModel(model, pageResult, request);
        model.addAttribute("pageTitle", "Всі замовлення (Admin)");

        return "orders";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/my")
    public String myClientOrders(Model model, Principal principal, OrderSearchRequest request) {
        Page<OrderDTO> pageResult = orderService.getMyOrders(principal.getName(), request);
        populateModel(model, pageResult, request);
        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/my")
    public String myEmployeeOrders(Model model, Principal principal, OrderSearchRequest request) {
        Page<OrderDTO> pageResult = orderService.getMyWorkOrders(principal.getName(), request);
        populateModel(model, pageResult, request);
        model.addAttribute("pageTitle", "Мої замовлення");
        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/available")
    public String availableOrders(Model model, OrderSearchRequest request) {
        if (request.getStatuses() == null || request.getStatuses().isEmpty()) {
            request.setStatuses(List.of(OrderStatus.NEW));
        }

        Page<OrderDTO> pageResult = orderService.getFilteredOrders(request);

        populateModel(model, pageResult, request);
        model.addAttribute("pageTitle", "Доступні замовлення");

        return "orders";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{id}")
    public String getAllOrdersByEmployee(@PathVariable("id") Long id, Model model, OrderSearchRequest request) {
        request.setEmployeeId(id);
        Page<OrderDTO> pageResult = orderService.getFilteredOrders(request);

        populateModel(model, pageResult, request);
        model.addAttribute("pageTitle", "Замовлення працівника #" + id);

        return "orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/client/{id}")
    public String getAllOrdersByClient(@PathVariable("id") Long id, Model model, OrderSearchRequest request) {
        request.setClientId(id);
        Page<OrderDTO> pageResult = orderService.getFilteredOrders(request);

        populateModel(model, pageResult, request);
        model.addAttribute("pageTitle", "Замовлення клієнта #" + id);

        return "orders";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'CLIENT')")
    @GetMapping("/{id}/details")
    public String getOrderDetails(@PathVariable("id") Long id, Model model, Principal principal, Authentication authentication) {
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


    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/take")
    public String takeOrder(@PathVariable Long id, Principal principal,
                            OrderSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            orderService.takeOrder(id, principal.getName());
            redirectAttributes.addAttribute("msg", "order.take.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "order.take.error");
        }

        fillRedirectAttributes(redirectAttributes, request);
        return "redirect:/orders/available";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/refuse")
    public String refuseOrder(@PathVariable Long id, Principal principal,
                              OrderSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, OrderStatus.NEW, principal.getName());
            redirectAttributes.addAttribute("msg", "order.refuse.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "order.refuse.error");
        }

        fillRedirectAttributes(redirectAttributes, request);
        return "redirect:/orders/employee/my";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/canceled")
    public String canceledOrder(@PathVariable Long id, Principal principal,
                                OrderSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            orderService.refund(id, principal.getName());
            redirectAttributes.addAttribute("msg", "order.cancel.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "order.cancel.error");
        }

        fillRedirectAttributes(redirectAttributes, request);
        return "redirect:/orders/employee/my";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/deliver")
    public String deliverOrder(@PathVariable Long id, Principal principal,
                               OrderSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            orderService.deliver(id, principal.getName());
            redirectAttributes.addAttribute("msg", "order.deliver.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "order.deliver.error");
        }

        fillRedirectAttributes(redirectAttributes, request);
        return "redirect:/orders/employee/my";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/client/canceled")
    public String canceledOrderByClient(@PathVariable Long id, Principal principal,
                                        OrderSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            orderService.refund(id, principal.getName());
            redirectAttributes.addAttribute("msg", "order.cancel.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "order.cancel.error");
        }

        fillRedirectAttributes(redirectAttributes, request);
        return "redirect:/orders/client/my";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/add")
    public String addToBasket(@RequestParam("bookId") Long bookId,
                              @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                              Principal principal, RedirectAttributes redirectAttributes) {
        cartService.addItemToCart(principal.getName(), bookId, quantity);
        redirectAttributes.addAttribute("msg", "basket.add.success");
        return "redirect:/books/" + bookId;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/update")
    public String updateBasketQuantity(@RequestParam("bookId") Long bookId,
                                       @RequestParam("quantity") Integer quantity,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "5") int size,
                                       Principal principal) {
        cartService.updateQuantity(principal.getName(), bookId, quantity);
        return "redirect:/orders/basket?page=" + page + "&size=" + size;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/basket/remove")
    public String removeFromBasket(@RequestParam("bookId") Long bookId, Principal principal,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "5") int size,
                                   RedirectAttributes redirectAttributes) {
        cartService.removeItem(principal.getName(), bookId);
        redirectAttributes.addAttribute("msg", "basket.remove.success");
        return "redirect:/orders/basket?page=" + page + "&size=" + size;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/basket")
    public String showBasket(Model model, Principal principal,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size) {
        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<BookItemDTO> cartPage = cartService.getCartItems(email, pageable);
        BigDecimal totalPrice = cartService.getTotalPrice(email);

        model.addAttribute("items", cartPage.getContent());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("currentPage", cartPage.getNumber());
        model.addAttribute("totalPages", cartPage.getTotalPages());
        model.addAttribute("totalItems", cartPage.getTotalElements());
        model.addAttribute("size", size);

        return "basket";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/create")
    public String createOrder(Principal principal, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        List<BookItemDTO> allItems = cartService.getAllCartItems(email);

        if (allItems.isEmpty()) {
            redirectAttributes.addAttribute("error", "order.create.empty");
            return "redirect:/orders/basket";
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBookItems(new ArrayList<>(allItems));

        try {
            orderService.addOrder(orderDTO, email);
            cartService.clearCart(email);
            redirectAttributes.addAttribute("msg", "order.create.success");
        } catch (NotEnoughMoneyException e) {
            redirectAttributes.addAttribute("error", "not_enough_money");
            return "redirect:/orders/basket";
        }

        return "redirect:/orders/client/my";
    }



    private void populateModel(Model model, Page<OrderDTO> page, OrderSearchRequest request) {
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("size", request.getSize());
        model.addAttribute("sortField", request.getSortField());
        model.addAttribute("sortDir", request.getSortDir());
        model.addAttribute("reverseSortDir", request.getReverseSortDir());
        model.addAttribute("search", request.getSearch());

        model.addAttribute("minPrice", request.getMinPrice());
        model.addAttribute("maxPrice", request.getMaxPrice());
        model.addAttribute("dateFrom", request.getDateFrom());
        model.addAttribute("dateTo", request.getDateTo());
        model.addAttribute("selectedStatuses", request.getStatuses());
    }


    private void fillRedirectAttributes(RedirectAttributes redirectAttributes, OrderSearchRequest request) {
        redirectAttributes.addAttribute("page", request.getPage());
        redirectAttributes.addAttribute("size", request.getSize());
        redirectAttributes.addAttribute("sortField", request.getSortField());
        redirectAttributes.addAttribute("sortDir", request.getSortDir());

        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            redirectAttributes.addAttribute("search", request.getSearch());
        }
        if (request.getDateFrom() != null) {
            redirectAttributes.addAttribute("dateFrom", request.getDateFrom());
        }
        if (request.getDateTo() != null) {
            redirectAttributes.addAttribute("dateTo", request.getDateTo());
        }
        if (request.getMinPrice() != null) {
            redirectAttributes.addAttribute("minPrice", request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            redirectAttributes.addAttribute("maxPrice", request.getMaxPrice());
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            redirectAttributes.addAttribute("statuses", request.getStatuses());
        }
    }
}