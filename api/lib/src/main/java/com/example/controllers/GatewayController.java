/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.controllers;

import com.example.clients.OrdersClient;
import com.example.clients.UsersClient;
import com.example.models.Item;
import com.example.models.Order;
import com.example.models.User;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.validation.Validated;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller("/api") // <1>
@Validated
@ExecuteOn(TaskExecutors.IO) // <2>
class GatewayController {

    private final OrdersClient orderClient;
    private final UsersClient userClient;

    GatewayController(OrdersClient orderClient, UsersClient userClient) {
        this.orderClient = orderClient;
        this.userClient = userClient;
    }

    @Get("/users/{id}") // <3>
    User getUserById(int id) {
        return userClient.getById(id);
    }

    @Get("/orders/{id}") // <4>
    Order getOrdersById(int id) {
        Order order = orderClient.getOrderById(id);
        return new Order(order.id(), null, getUserById(order.userId()), order.items(), order.itemIds(), order.total());
    }

    @Get("/items/{id}") // <5>
    Item getItemsById(int id) {
        return orderClient.getItemsById(id);
    }

    @Get("/users") // <6>
    List<User> getUsers() {
        return userClient.getUsers();
    }

    @Get("/items") // <7>
    List<Item> getItems() {
        return orderClient.getItems();
    }

    @Get("/orders") // <8>
    List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        orderClient.getOrders().forEach(x-> orders.add(new Order(x.id(), null, getUserById(x.userId()), x.items(), x.itemIds(), x.total())));
        return orders;
    }

    @Post("/orders") // <9>
    Order createOrder(@Body @Valid Order order) {
        User user = getUserById(order.userId());
        if (user == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with id %s doesn't exist", order.userId()));
        }
        Order createdOrder = orderClient.createOrder(order);
        return new Order(createdOrder.id(), null, user, createdOrder.items(), createdOrder.itemIds(), createdOrder.total());
    }

    @Post("/users")  // <10>
    User createUser(@Body @NonNull User user) {
        return userClient.createUser(user);
    }

}
