package edu.hit.fmpmm.service.web.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hit.fmpmm.domain.web.User;
import edu.hit.fmpmm.dto.Result;

import edu.hit.fmpmm.dto.UserDTO;
import edu.hit.fmpmm.mapper.UserMapper;
import edu.hit.fmpmm.service.web.UserMsgService;
import edu.hit.fmpmm.service.web.UserService;
import edu.hit.fmpmm.util.Constant;
import edu.hit.fmpmm.util.StringUtil;
import edu.hit.fmpmm.util.UserHolder;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final MultipartConfigElement multipartConfigElement;
    private final UserMsgService userMsgService;

    @Autowired
    public UserServiceImpl(MultipartConfigElement multipartConfigElement, UserMsgService userMsgService) {
        this.multipartConfigElement = multipartConfigElement;
        this.userMsgService = userMsgService;
    }

    /**
     * 普通用户的登录功能
     *
     * @param user     DTO层的普通用户表示
     * @param session  session
     * @param request  request
     * @param response response
     * @return 登录的结果
     */
    @Override
    public Result login(UserDTO user, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        // 获取用户的输入
        String phone = user.getPhone();
        if (!StringUtil.isPhoneLegal(phone)) {
            return Result.fail("手机号不正确");
        }
        String password = user.getPassword();
        if (!StringUtil.isPwdLegal(password)) {
            return Result.fail("密码不正确");
        }
        // 查看账号是否是存在的
        User u = query().eq("phone", phone).one();
        if (u == null) {
            return Result.fail("该账号是不存在的");
        }
        // 验证密码是否正确
        if (!password.equals(u.getPassword())) {
            return Result.fail("密码错误");
        }
        // 将登录信息保存到session
        UserDTO userDTO = BeanUtil.copyProperties(u, UserDTO.class);
        session.setAttribute(Constant.SESSION4USER, userDTO);
        // 保存到Cookie
        Cookie cookie = new Cookie(Constant.COOKIE4USER, userDTO.getId().toString());
        // 设置cookie的有效期
        cookie.setMaxAge(Constant.COOKIE4USER_AGE);
        // 设置当前项目下都携带这个cookie
        cookie.setPath("/");  // request.getContextPath()
        // 向客户端发送cookie
        response.addCookie(cookie);
//        UserHolder.setUser(userDTO);
        return Result.success(u);
    }

    /**
     * 普通用户的注册功能
     *
     * @param userDTO DTO层的用户表示
     * @return 注册的结果
     */
    @Override
    public Result register(UserDTO userDTO) {
        User user = new User();
        user.setPhone(userDTO.getPhone());
        user.setPassword(userDTO.getPassword());
        String userDTONickname = userDTO.getNickname();
        if (userDTONickname != null && !userDTONickname.isEmpty()) {
            user.setNickname(userDTONickname);
        } else {
            user.setNickname(Constant.USER_NICKNAME_PREFIX + StringUtil.randomString(5));
        }
        System.out.println("user:" + user);
        // 查询用户是否已经注册过
        User hasUser = query().eq("phone", user.getPhone()).one();
        if (hasUser != null) {
            System.out.println("hello");
            return Result.fail("已经注册过了");
        }
        try {
            boolean saved = save(user);
            if (saved) {
                return Result.success();
            }
        } catch (Exception e) {
            return Result.fail("注册失败T_T");
        }
        return Result.fail("注册失败T_T");
    }

    public Result logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        deleteUserSessionAndCookie(session, response);
        return Result.success();
    }

    private void deleteUserSessionAndCookie(HttpSession session, HttpServletResponse response) {
        session.removeAttribute(Constant.SESSION4USER);  // 删除session中的信息
        // 销毁Cookie
        Cookie cookie = new Cookie(Constant.COOKIE4USER, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");  // request.getContextPath()
        response.addCookie(cookie);  // 向客户端发送cookie
    }

    /**
     * 将用户上传的场景文件做临时保存配置
     *
     * @param file 场景文件
     * @return 保存是否成功
     */
    @Override
    public Result uploadSceneFile(MultipartFile file) {
        // 临时保存场景文件，创建用户手机号的文件夹，在文件夹里保存文件
        // 获取用户信息 UserHolder
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请检查登录状态");
        }
        String userFolderName = user.getPhone();
        String location = multipartConfigElement.getLocation();
        File locationFolder = new File(location);
        if (!locationFolder.exists()) {
            boolean makeFlag = locationFolder.mkdirs();
            if (!makeFlag) {
                return Result.fail("上传场景文件失败（创建系统文件夹失败）");
            }
        }

        File fileFolder = new File(locationFolder.getAbsolutePath() + File.separator + userFolderName);
        if (!fileFolder.exists()) {
            boolean makeFlag = fileFolder.mkdirs();
            if (!makeFlag) {
                return Result.fail("上传场景文件失败（创建用户文件夹失败）");
            }
        }
        try {
            Path path = Paths.get(fileFolder.getPath(), file.getOriginalFilename());
            Files.write(path, file.getBytes());
            return Result.success(path);
        } catch (IOException e) {
            // throw new RuntimeException(e);
            return Result.fail("上传场景文件失败");
        }
    }

    @Override
    public Result updateUserById(UserDTO user) {
        User u = new User();
        u.setId(user.getId());
        u.setPhone(user.getPhone());
        u.setPassword(user.getPassword());
        u.setNickname(user.getNickname());
        boolean updated = updateById(u);
        if (updated) {
            return Result.success();
        }
        return Result.fail("更新用户信息失败!");
    }

    @Override
    @Transactional("mysqlFmpmmTransactionManager")
    public Result deleteUser(Integer id, HttpSession session, HttpServletResponse response) {
        try {
            System.out.println("hello");
            // 先删除和用户相关的所有消息
            boolean deleted = userMsgService.deleteAllMsgByUser(id);
            if (!deleted) {
                return Result.fail("注销失败，删除用户数据失败！");
            }
            // 注销账户
            boolean removed = removeById(id);
            if (removed) {
                deleteUserSessionAndCookie(session, response);
                return Result.success();
            } else {
                return Result.fail("注销失败！");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.fail("注销失败！" + e.getMessage());
        }
    }
}
