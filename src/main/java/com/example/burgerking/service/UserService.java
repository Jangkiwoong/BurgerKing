package com.example.burgerking.service;

import com.example.burgerking.dto.LoginRequestDto;
import com.example.burgerking.dto.SignupRequestDto;
import com.example.burgerking.entity.User;
import com.example.burgerking.entity.UserRoleEnum;
import com.example.burgerking.jwt.JwtUtil;
import com.example.burgerking.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private static final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    @Transactional
    public ResponseEntity signup(SignupRequestDto signupRequestDto) {
        String username = signupRequestDto.getUsername();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        String emailid = signupRequestDto.getEmailid();
        String phoneNumber = signupRequestDto.getPhonenumber();

        // 회원 중복 확인
        Optional<User> findEmail = userRepository.findByEmail(emailid);
        if (findEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.(email일치)");
        }
        Optional<User> fnidPhonNumber = userRepository.findByPhonenumber(phoneNumber);
        if (fnidPhonNumber.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.(phoneNumber일치)");
        }
        Optional<User> fnidUsername = userRepository.findByUsername(username);
        if (fnidUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.(username일치)");
        }


        //사용자 ROLE확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (signupRequestDto.isAdmin()) {
            if (!signupRequestDto.getAdmintoken().equals(ADMIN_TOKEN)) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = new User(signupRequestDto,password, role);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("회원가입 성공");
    }

    @Transactional(readOnly = true)
    public ResponseEntity login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        // 사용자 확인
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );


        // 비밀번호 확인
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw  new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getUsername(), user.getRole()));

        return ResponseEntity.status(HttpStatus.OK).body("로그인 성공");
    }

}

