package barinov.task.management.system.util;

import barinov.task.management.system.dto.PersonDTO;
import barinov.task.management.system.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonDTOValidator implements Validator {

    private final PersonDetailsService personDetailsService;

    @Autowired
    public PersonDTOValidator(PersonDetailsService personDetailsService) {
        this.personDetailsService = personDetailsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonDTO personDTO = (PersonDTO) target;

        if(personDetailsService.containsPersonWithEmail(personDTO.getEmail()))
            errors.rejectValue("email", "","Человек с такой почтой существует");
    }
}
