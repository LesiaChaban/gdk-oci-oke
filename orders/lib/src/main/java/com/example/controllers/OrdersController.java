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

import com.example.models.Item;
import com.example.models.Order;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/orders")  // <1>
@Secured(SecurityRule.IS_AUTHENTICATED)  // <2>
class OrdersController {

    private final List<Order> orders = new ArrayList<>();

    @Get("/{id}")  // <3>
    public Order findById(int id) {
        return orders.stream()
                .filter(it -> it.id().equals(id))
                .findFirst().orElse(null);
    }

    @Get  // <4>
    public List<Order> getOrders() {
        return orders;
    }

    @Post  // <5>
    public Order createOrder(@Body @Valid Order order) {
        if (order.itemIds() == null || order.itemIds().isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Items must be supplied");
        }

        List<Item> items = order.itemIds().stream().map(
                x -> Item.items.stream().filter(
                        y -> y.id().equals(x)
                ).findFirst().orElseThrow(
                        () -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("Item with id %s doesn't exist", x))
                )
        ).collect(Collectors.toList());

        BigDecimal total = items.stream().map(Item::price).reduce(BigDecimal::add).orElse(new BigDecimal("0"));
        Order newOrder = new Order(orders.size() + 1, order.userId(), items, null, total);

        orders.add(newOrder);
        return newOrder;
    }

}
