package ru.nasedkin.marvelapidemo.srevices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.nasedkin.marvelapidemo.models.Character;
import ru.nasedkin.marvelapidemo.models.Comics;
import ru.nasedkin.marvelapidemo.repositories.CharacterRepository;
import ru.nasedkin.marvelapidemo.repositories.ComicsRepository;
import ru.nasedkin.marvelapidemo.util.DataNotAddedException;
import ru.nasedkin.marvelapidemo.util.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ComicsService {

    private final ComicsRepository repository;
    private final CharacterRepository characterRepository;

    @Autowired
    public ComicsService(ComicsRepository repository, CharacterRepository characterRepository) {
        this.repository = repository;
        this.characterRepository = characterRepository;
    }

    public List<Comics> findAll(PageRequest pageRequest) {
        Page<Comics> comicsPage = repository.findAll(pageRequest);
        List<Comics> comics = comicsPage.getContent();
        if (comics.isEmpty())
            throw new ResourceNotFoundException("Comics not found.");
        return comics;
    }

    public Comics findById(int id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Comics with id " + "'" + id + "'" + " not found."));
    }

    public Set<Character> findComicsCharacters(int id) {
        Comics comics = this.findById(id);
        Set<Character> characters = comics.getCharacters();
        if (characters.isEmpty())
            throw new ResourceNotFoundException("Comics with id " + "'" + id + "'" + " has 0 characters.");
        return characters;
    }

    public void save(Comics comics) {
        repository.save(comics);
    }

    public void linkWithCharacters(int comicsId, List<Integer> charactersId) {
        if (charactersId == null || charactersId.isEmpty())
            throw new DataNotAddedException("Nothing to link with.");

        Comics comics = findById(comicsId);
        for (int i : charactersId) {
            comics.getCharacters().add(characterRepository.findById(i).orElseThrow(() -> new ResourceNotFoundException("Character with id: " + i + " not found.")));
        }
        repository.save(comics);
    }

    public Page<Comics> search(Specification<Comics> spec, Pageable page) {
        return repository.findAll(spec, page);
    }

    public void setThumbnail(int id, MultipartFile file) {
        Comics comics = findById(id);
        try {
            comics.setThumbnailName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
            comics.setThumbnailType(file.getContentType());
            comics.setThumbnail(file.getBytes());
            comics.setThumbnailUri("http://localhost:8080/v1/public/comics/" + id + "/thumbnail");
            repository.save(comics);
        } catch (Exception e) {
            throw new DataNotAddedException("Thumbnail not added.");
        }
    }

    public ResponseEntity<Resource> getThumbnail(int id) {
        Comics comics = findById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(comics.getThumbnailType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "thumbnail; thumbnail name=\"" + comics.getThumbnailName() + "\"")
                .body(new ByteArrayResource(comics.getThumbnail()));
    }
}
