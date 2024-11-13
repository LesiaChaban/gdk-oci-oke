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

package com.example;

import com.example.auth.Credentials;
import com.example.models.User;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest // <1>
class UsersControllerTest {

    @Inject
    UsersClient usersClient;

    @Inject
    Credentials credentials;

    @Test
    void testUnauthorized() {
        HttpClientException exception = assertThrows(HttpClientException.class, () -> usersClient.getUsers(""));
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void getUserThatDoesntExists() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((credentials.username() + ":" + credentials.password()).getBytes());
        User retriedUser = usersClient.getById(authHeader, 100);
        assertNull(retriedUser);
    }

    @Test
    void multipleUserInteraction() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((credentials.username() + ":" + credentials.password()).getBytes());

        String firstName = "firstName";
        String lastName = "lastName";
        String username = "username";

        User user = new User(0 ,firstName, lastName, username);

        User createdUser = usersClient.createUser(authHeader, user);

        assertEquals(firstName, createdUser.firstName());
        assertEquals(lastName, createdUser.lastName());
        assertEquals(username, createdUser.username());
        assertNotNull(createdUser.id());

        User retriedUser = usersClient.getById(authHeader, createdUser.id());

        assertEquals(firstName, retriedUser.firstName());
        assertEquals(lastName, retriedUser.lastName());
        assertEquals(username, retriedUser.username());

        List<User> users = usersClient.getUsers(authHeader);
        assertNotNull(users);
        assertTrue(users.stream()
                .map(User::username)
                .anyMatch(name -> name.equals(username)));

    }

    @Test
    void createSameUserTwice() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((credentials.username() + ":" + credentials.password()).getBytes());

        String firstName = "SameUserFirstName";
        String lastName = "SameUserLastName";
        String username = "SameUserUsername";

        User user = new User(0 ,firstName, lastName, username);

        User createdUser = usersClient.createUser(authHeader, user);

        assertEquals(firstName, createdUser.firstName());
        assertEquals(lastName, createdUser.lastName());
        assertEquals(username, createdUser.username());
        assertNotNull(createdUser.id());
        assertNotEquals(createdUser.id(), 0);

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> usersClient.createUser(authHeader, user));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getResponse().getBody(String.class).orElse("").contains("User with provided username already exists"));

    }
}
