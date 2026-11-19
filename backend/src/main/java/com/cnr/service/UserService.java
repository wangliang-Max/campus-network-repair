package com.cnr.service;

import com.cnr.common.PageResult;
import com.cnr.dto.UserQuery;
import com.cnr.vo.UserVO;

import java.util.List;

public interface UserService {

    PageResult<UserVO> page(UserQuery query);

    UserVO setEnabled(Long id, boolean enabled);

    List<UserVO> technicians();

    UserVO currentUser();

    void changePassword(String oldPassword, String newPassword);
}
