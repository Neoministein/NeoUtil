package com.neo.util.helidon.security.impl.key;

import java.security.PublicKey;
import java.util.Date;

public class JWTPublicKey extends JWTKey{

    public JWTPublicKey(String id, PublicKey publicKey, Date expirationDate) {
        super(id, publicKey, expirationDate);
    }

    @Override
    public PublicKey getKey() {
        return (PublicKey) super.getKey();
    }
}
