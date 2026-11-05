package com.cnr.service;


import com.cnr.dto.LoginDTO;
import com.cnr.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);
}
