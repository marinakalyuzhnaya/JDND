package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    private OrderController orderController;
    private OrderRepository orderRepository = mock(OrderRepository.class);
    private UserRepository userRepository = mock(UserRepository.class);

    private static List<UserOrder> createUserOrders() {
        UserOrder userOrder = UserOrder.createFromCart(createUser().getCart());
        return Lists.list(userOrder);
    }

    public static Item createItem(long id){
        Item item = new Item();
        item.setId(id);
        item.setPrice(BigDecimal.valueOf(id * 2000));
        item.setName("TestItem " + item.getId());
        item.setDescription("Some test Item Description ");
        return item;
    }

    public static Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.addItem(createItem(123));
        return cart;
    }


    public static User createUser() {
        User user = new User();
        user.setId(123);
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setCart(createCart(user));
        return user;
    }

    @Before
    public void setup() {
        orderController = new OrderController();
        TestUtils.injectObject(orderController, "orderRepository", orderRepository);
        TestUtils.injectObject(orderController, "userRepository", userRepository);
        when(userRepository.findByUsername("testUser")).thenReturn(createUser());
        when(orderRepository.findByUser(any())).thenReturn(createUserOrders());
    }

    @Test
    public void test_submit() {
        BigDecimal expected = new BigDecimal(246000);
        final ResponseEntity<UserOrder> response = orderController.submit("testUser");
        UserOrder userOrder = response.getBody();
        assertEquals(response.getStatusCodeValue(), HttpStatus.OK.value());
        assertNotNull(userOrder);
        assertEquals(userOrder.getUser().getUsername(), "testUser");
        assertEquals(userOrder.getItems().size(), 1);
        assertEquals(userOrder.getTotal(), expected);
    }

    @Test
    public void test_get_orders_for_user() {
        final ResponseEntity<List<UserOrder>> ordersForUser = orderController.getOrdersForUser("testUser");
        assertNotNull(ordersForUser);
        assertEquals(200, ordersForUser.getStatusCodeValue());
        List<UserOrder> orders = ordersForUser.getBody();
        assertNotNull(orders);
    }

    @Test
    public void submit_order_userName_not_found() {
        final ResponseEntity<UserOrder> response = orderController.submit("");
        assertNotNull(response);
        assertEquals(response.getStatusCodeValue(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void test_get_orders_for_userName_not_found() {
        final ResponseEntity<List<UserOrder>> ordersForUser = orderController.getOrdersForUser("");
        assertNotNull(ordersForUser);
        assertEquals(404, ordersForUser.getStatusCodeValue());
    }
}
