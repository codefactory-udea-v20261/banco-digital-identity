package com.udea.bancodigital.auth.domain.port.in;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;

public interface AuthPort {
    LoginResponseDto login(LoginRequestDto request);
    void logout(LogoutRequestDto request);
}
