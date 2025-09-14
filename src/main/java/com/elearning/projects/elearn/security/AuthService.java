package com.elearning.projects.elearn.security;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.elearning.projects.elearn.dto.LoginDto;
import com.elearning.projects.elearn.dto.SignUpRequestDto;
import com.elearning.projects.elearn.dto.UserDto;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.repository.UserRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    // public UserDto signUp(SignUpRequestDto signUpRequestDto) {

    // User user =
    // userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

    // if (user != null) {
    // throw new RuntimeException("User is already present with same email id");
    // }

    // User newUser = modelMapper.map(signUpRequestDto, User.class);
    // newUser.setRoles(Set.of(Role.GUEST));
    // newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
    // newUser = userRepository.save(newUser);

    // return modelMapper.map(newUser, UserDto.class);
    // }

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        // Check if user already exists
        User existingUser = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (existingUser != null) {
            throw new RuntimeException("User already exists with email: " + signUpRequestDto.getEmail());
        }

        // Validate role selection
        if (signUpRequestDto.getRole() != Role.HR && signUpRequestDto.getRole() != Role.EMPLOYEE) {
            throw new RuntimeException("Invalid role selection. Please select either INSTRUCTOR or STUDENT");
        }

        // Create new user
        User newUser = modelMapper.map(signUpRequestDto, User.class);

        // Set the selected role (instead of hardcoded GUEST)
        newUser.setRoles(Set.of(signUpRequestDto.getRole()));

        // Encode password
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        // Save user
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()));

        User user = (User) authentication.getPrincipal();

        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
    }

    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return jwtService.generateAccessToken(user);
    }

}
