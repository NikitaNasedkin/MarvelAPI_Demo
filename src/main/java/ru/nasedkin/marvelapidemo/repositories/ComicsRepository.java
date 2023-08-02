package ru.nasedkin.marvelapidemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.nasedkin.marvelapidemo.models.Comics;

@Repository
public interface ComicsRepository extends JpaRepository<Comics, Integer>, JpaSpecificationExecutor<Comics> {
}
