package com.epam.rd.autocode.spring.project;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
class FullIntegrationTest {

    @Autowired private ClientService clientService;
    @Autowired private EmployeeService employeeService;
    @Autowired private BookService bookService;
    @Autowired private OrderService orderService;

    // Інжектимо репозиторії, щоб перевіряти "внутрощі" бази
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private OrderRepository orderRepository;

    @Test
    void fullScenarioTest() {
        // ==========================================
        // 1. Створення Книг
        // ==========================================
        System.out.println("--- 1. Додаємо книги ---");

        BookDTO bookDTO1 = new BookDTO(0L,
                "Harry Potter", "Fantasy", AgeGroup.TEEN, BigDecimal.valueOf(100.00),
                LocalDate.now(), "Rowling", 500, "Magic", "Desc", Language.ENGLISH
        );
        bookService.addBook(bookDTO1);

        BookDTO bookDTO2 = new BookDTO(1L,
                "Java Guide", "Education", AgeGroup.ADULT, BigDecimal.valueOf(50.00),
                LocalDate.now(), "Schildt", 800, "Code", "Desc", Language.ENGLISH
        );
        bookService.addBook(bookDTO2);

        // ПЕРЕВІРКА ЧЕРЕЗ РЕПОЗИТОРІЙ (Бо в DTO немає ID)
        Optional<Book> bookInDb = bookRepository.findByNameIgnoreCase("Harry Potter"); // Або findByNameIgnoreCase
        Assertions.assertTrue(bookInDb.isPresent(), "Книга має з'явитися в базі");
        Assertions.assertNotNull(bookInDb.get().getId(), "У сутності в базі має бути ID");

        // ==========================================
        // 2. Реєстрація Клієнта
        // ==========================================
        System.out.println("--- 2. Реєструємо клієнта ---");

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setEmail("client@test.com");
        clientDTO.setPassword("pass123");
        clientDTO.setName("Ivan Client");
        clientDTO.setBalance(BigDecimal.valueOf(1000.00));

        clientService.addClient(clientDTO);

        // ПЕРЕВІРКА ЧЕРЕЗ РЕПОЗИТОРІЙ
        User clientInDb = userRepository.findByEmail("client@test.com").orElseThrow();
        Assertions.assertNotNull(clientInDb.getId());
        Assertions.assertEquals(0, new BigDecimal("1000.00").compareTo(clientInDb.getClientProfile().getBalance()));

        // ==========================================
        // 3. Реєстрація Працівника
        // ==========================================
        System.out.println("--- 3. Реєструємо працівника ---");

        EmployeeDTO empDTO = new EmployeeDTO();
        empDTO.setEmail("employee@test.com");
        empDTO.setPassword("workpass");
        empDTO.setName("Petro Worker");
        empDTO.setPhone("+380991234567");
        empDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        employeeService.addEmployee(empDTO);

        // ПЕРЕВІРКА
        User empInDb = userRepository.findByEmail("employee@test.com").orElseThrow();
        Assertions.assertNotNull(empInDb.getId());

        // ==========================================
        // 4. Створення Замовлення
        // ==========================================
        System.out.println("--- 4. Робимо замовлення ---");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail("client@test.com");
        orderDTO.setEmployeeEmail("employee@test.com");

        // Купуємо: 2 Гаррі Поттера (200 грн) + 1 Java (50 грн) = 250 грн
        List<BookItemDTO> items = List.of(
                new BookItemDTO("Harry Potter", 2),
                new BookItemDTO("Java Guide", 1)
        );
        orderDTO.setBookItems(items);

        orderService.addOrder(orderDTO, "client@test.com" );

        // ==========================================
        // 5. Фінальні перевірки
        // ==========================================
        System.out.println("--- 5. Перевіряємо результати ---");

        // 1. Чи змінився баланс клієнта в базі? (1000 - 250 = 750)
        User updatedClient = userRepository.findByEmail("client@test.com").get();
        BigDecimal currentBalance = updatedClient.getClientProfile().getBalance();

        Assertions.assertEquals(0, new BigDecimal("750.00").compareTo(currentBalance),
                "Баланс мав зменшитися на суму замовлення");

        // 2. Чи з'явилося замовлення в базі?
        List<Order> orders = orderRepository.findAllByClient(updatedClient);
        Assertions.assertFalse(orders.isEmpty(), "Замовлення має бути в базі");

        Order savedOrder = orders.get(0);
        Assertions.assertEquals(0, new BigDecimal("250.00").compareTo(savedOrder.getPrice()),
                "Ціна замовлення має бути 250");

        Assertions.assertEquals(2, savedOrder.getBookItems().size(), "Має бути 2 позиції в чеку");
    }
}