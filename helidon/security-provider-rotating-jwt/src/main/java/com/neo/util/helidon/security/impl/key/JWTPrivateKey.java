package com.neo.util.helidon.security.impl.key;

import java.security.PrivateKey;
import java.util.Date;

public class JWTPrivateKey extends JWTKey {

    public JWTPrivateKey(String id, PrivateKey privateKey, Date expirationDate) {
        super(id, privateKey, expirationDate);
    }

    @Override
    public PrivateKey getKey() {
        return (PrivateKey) super.getKey();
    }
}
