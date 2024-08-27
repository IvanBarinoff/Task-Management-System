package barinov.task.management.system.controllers;

import barinov.task.management.system.dto.PersonDTO;
import barinov.task.management.system.exceptions.PersonBadCredentialsException;
import barinov.task.management.system.exceptions.PersonNotCreatedException;
import barinov.task.management.system.models.Person;
import barinov.task.management.system.security.JWTUtil;
import barinov.task.management.system.services.RegistrationService;
import barinov.task.management.system.util.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PersonDTOValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(PersonDTOValidator personValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper, AuthenticationManager authenticationManager) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/registration")
    public Map<String, String> performRegistration(@RequestBody @Valid PersonDTO personDTO,
                                      BindingResult bindingResult) {

        personValidator.validate(personDTO, bindingResult);

        if(bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();

            for(FieldError fieldError : errors) {
                errorMsg.append(fieldError.getField())
                        .append(" - ").append(fieldError.getDefaultMessage())
                        .append(';');
            }

            throw new PersonNotCreatedException(errorMsg.toString());
        }

        Person person = convertToPerson(personDTO);

        registrationService.register(person);

        String token = jwtUtil.generateToken(person.getEmail());

        return Map.of("jwt-token", token);
    }

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody PersonDTO personDTO) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(personDTO.getEmail(),
                        personDTO.getPassword());

        try {
            authenticationManager.authenticate(authToken);
        }catch (BadCredentialsException e) {
            throw new PersonBadCredentialsException();
        }

        String newToken = jwtUtil.generateToken(personDTO.getEmail());
        return Map.of("jwt-token", newToken);
    }

    public Person convertToPerson(PersonDTO personDTO) {
        return this.modelMapper.map(personDTO, Person.class);
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handlerException(PersonNotCreatedException exception) {
        PersonErrorResponse response = new PersonErrorResponse(
                exception.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handlerException(PersonBadCredentialsException exception) {
        PersonErrorResponse response = new PersonErrorResponse(
                "Неверные логин или пароль",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
