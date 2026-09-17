package com.neo.util.api.request.user;

import java.security.Principal;
import java.util.Set;

public interface RolePrincipal extends Principal {

    Set<String> getRoles();
}
