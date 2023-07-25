package com.neo.util.framework.api.request;

import com.neo.util.framework.api.security.RolePrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class UserRequestDetailsTest {

    UserRequestDetails subject;

    @BeforeEach
    void before() {
        subject = new AbstractUserRequestDetails("", "", new Context()) {};
    }


    @Test
    void rolesMethodsTest() {
        Set<String> roles = Set.of("BASIC_ROLE", "ADVANCED_ROLE");

        //Checking false if user is not set
        Assertions.assertFalse(subject.isInRole("BASIC_ROLE"));
        Assertions.assertFalse(subject.hasOneOfTheRoles(Set.of("BASIC_ROLE", "ADVANCED_ROLE")));
        Assertions.assertFalse(subject.hasOneOfTheRoles(Set.of()));
        Assertions.assertFalse(subject.hasAllRoles(Set.of("BASIC_ROLE", "ADVANCED_ROLE")));
        Assertions.assertFalse(subject.hasOneOfTheRoles(Set.of()));

        //Setting false
        subject.setUserIfPossible(new User("", roles));

        //Checking all cases where it should be true
        Assertions.assertTrue(subject.isInRole("BASIC_ROLE"));
        Assertions.assertTrue(subject.hasOneOfTheRoles(Set.of("BASIC_ROLE", "ADVANCED_ROLE")));
        Assertions.assertTrue(subject.hasOneOfTheRoles(Set.of("BASIC_ROLE", "ADVANCED_ROLE", "SUPER_ROLE")));
        Assertions.assertTrue(subject.hasOneOfTheRoles(Set.of()));
        Assertions.assertTrue(subject.hasAllRoles(Set.of("BASIC_ROLE", "ADVANCED_ROLE")));
        Assertions.assertTrue(subject.hasAllRoles(Set.of()));

        //Checking all cases where it should be false
        Assertions.assertFalse(subject.isInRole("SUPER_ROLE"));
        Assertions.assertFalse(subject.hasOneOfTheRoles(Set.of("SUPER_ROLE", "MEGA_ROLE")));
        Assertions.assertFalse(subject.hasAllRoles(Set.of("SUPER_ROLE", "ADVANCED_ROLE")));

    }


    public record Context() implements RequestContext {
        @Override
        public String type() {
            return null;
        }
    }

    public record User(String name, Set<String> roles) implements RolePrincipal {

        @Override
        public Set<String> getRoles() {
            return roles;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
