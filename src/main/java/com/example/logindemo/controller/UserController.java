package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.dto.RegisterDTO;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块", description = "用户登录、注册、个人信息相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录，登录成功后返回 JWT token")
    @PostMapping("/login")
    public Result<String> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        if (user != null) {
            user.setPassword(null);
            String token = JwtUtil.generateToken(user);
            return Result.success(token);
        }
        return Result.error("用户名或密码错误");
    }

    @Operation(summary = "获取当前用户信息", description = "根据请求头中的 token 获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<User> info(@RequestHeader("Authorization") String token) {
        String username = JwtUtil.getUsername(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "用户注册", description = "用户填写用户名、密码、邮箱进行账号注册")
    @PostMapping("/register")
    public Result<String> register(@Valid @ModelAttribute RegisterDTO registerDTO) {
        boolean success = userService.register(registerDTO.getUsername(),
                registerDTO.getPassword(),registerDTO.getEmail());
        if (success) {
            return Result.success("注册成功");
        }
        return Result.error("用户名已存在");
    }

    @Operation(summary = "修改密码", description = "当前登录用户修改自己的登录密码")
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestHeader("Authorization") String token,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword){
        String username = JwtUtil.getUsername(token);
        boolean success = userService.changePassword(username,oldPassword,newPassword);
        if(success){
            return Result.success("修改密码成功");
        }
        return Result.error("旧密码错误或用户不存在");
    }

    @Operation(summary = "更新个人信息", description = "当前登录用户更新自己的用户名和邮箱")
    @PostMapping("/updateMyInfo")
    public Result<String> updateMyInfo(@RequestHeader("Authorization") String token,
                                       @RequestParam String username,
                                       @RequestParam String email){
        String currentUsername=JwtUtil.getUsername(token);
        User updatedUser=userService.updateCurrentUserInfo(currentUsername,username,email);
        if (updatedUser !=null) {
            updatedUser.setPassword(null);
            String newToken = JwtUtil.generateToken(updatedUser);
            return Result.success(newToken);
        }
        return Result.error("修改失败");
    }

    @Operation(summary = "注销账号", description = "当前登录用户注销自己的账号")
    @PostMapping("/deleteMyAccount")
    public Result<String> deleteMyAccount(@RequestHeader("Authorization") String token){
        String username = JwtUtil.getUsername(token);
        boolean success=userService.deleteCurrentUser(username);
        if(success){
            return Result.success("账号已注销");
        }
        return Result.error("注销失败");
    }

    @Operation(summary = "更新头像", description = "当前登录用户上传图片并更新个人头像")
    @PostMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestHeader("Authorization") String token,
                                       @RequestParam("file") MultipartFile file){
        String username=JwtUtil.getUsername(token);
        User user=userService.findByUsername(username);
        if(user==null){
            return Result.error("用户不存在");
        }
        if(file==null || file.isEmpty()){
            return Result.error("请选择要上传的文件");
        }
        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null){
            return Result.error("文件名无效");
        }
        String suffix=originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if(!suffix.matches("\\.(jpg|jpeg|png|gif)")){
            return Result.error("仅支持 jpg、jpeg、png、gif 格式的图片");
        }
        String newFileName = UUID.randomUUID().toString().replace("-","")+suffix;

        try{
            File destDir = new File("uploads");
            if(!destDir.exists()){
                destDir.mkdirs();
            }
            File destFile=new File(destDir,newFileName);
            file.transferTo(destFile);
        }catch(IOException e){
            return Result.error("文件保存失败：" + e.getMessage());
        }

        String avatarUrl="/uploads/"+newFileName;

        boolean success=userService.updateAvatar(user.getId(),avatarUrl);
        if(success){
            return Result.success(avatarUrl);
        }
        return Result.error("头像更新失败");
    }
}
