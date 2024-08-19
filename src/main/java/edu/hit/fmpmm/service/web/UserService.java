package edu.hit.fmpmm.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hit.fmpmm.domain.web.User;
import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    Result login(UserDTO user, HttpSession session, HttpServletRequest request, HttpServletResponse response);

    Result register(UserDTO user);

    Result logout(HttpSession session, HttpServletRequest request, HttpServletResponse response);

    Result uploadSceneFile(MultipartFile file);

    Result updateUserById(UserDTO user);

    Result deleteUser(Integer id, HttpSession session, HttpServletResponse response);
}
