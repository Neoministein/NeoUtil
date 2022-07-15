package com.neo.util.framework.api.security;

import java.security.Principal;
import java.util.Set;

public interface RolePrincipal extends Principal {

    Set<String> getRoles();
}
