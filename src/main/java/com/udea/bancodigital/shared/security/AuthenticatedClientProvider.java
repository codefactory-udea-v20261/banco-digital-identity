package com.udea.bancodigital.shared.security;

import java.util.UUID;

public interface AuthenticatedClientProvider {
    UUID getClienteId();
}
