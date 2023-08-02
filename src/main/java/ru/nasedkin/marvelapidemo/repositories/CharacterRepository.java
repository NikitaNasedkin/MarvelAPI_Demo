package ru.nasedkin.marvelapidemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.nasedkin.marvelapidemo.models.Character;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Integer>, JpaSpecificationExecutor<Character> {

}
