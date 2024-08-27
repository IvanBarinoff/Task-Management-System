package barinov.task.management.system.services;

import barinov.task.management.system.models.Person;
import barinov.task.management.system.repositories.PeopleRepository;
import barinov.task.management.system.security.PersonDetails;
import barinov.task.management.system.exceptions.PersonBadCredentialsException;
import barinov.task.management.system.exceptions.PersonNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonDetailsService implements UserDetailsService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonDetailsService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Person> person = peopleRepository.findByEmail(email);

        if(person.isEmpty())
            throw new PersonBadCredentialsException();

        return new PersonDetails(person.get());
    }

    public Person getPersonById(int id) {
        return peopleRepository.findById(id).orElseThrow(PersonNotFoundException::new);
    }

    public boolean containsPersonWithEmail(String email) {
        return peopleRepository.findByEmail(email).isPresent();
    }
}
