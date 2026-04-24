package com.udea.bancodigital.auth.domain.port.in;

import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;

public interface ProvisionClientAccessPort {
    boolean existsByEmail(String email);

    ProvisionClientAccessResponseDto provisionClientAccess(ProvisionClientAccessRequestDto request);
}
