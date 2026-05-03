package com.udea.bancodigital.auth.domain.port.in;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;
import com.udea.bancodigital.auth.application.dto.RefreshRequestDto;

public interface AuthPort {
    LoginResponseDto login(LoginRequestDto request);
    LoginResponseDto refresh(RefreshRequestDto request);
    void logout(LogoutRequestDto request);
}
