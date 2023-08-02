package ru.nasedkin.marvelapidemo.srevices;


import org.modelmapper.ModelMapper;
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
import ru.nasedkin.marvelapidemo.dto.CharacterDTO;
import ru.nasedkin.marvelapidemo.dto.ComicsDTO;
import ru.nasedkin.marvelapidemo.models.Character;
import ru.nasedkin.marvelapidemo.repositories.CharacterRepository;
import ru.nasedkin.marvelapidemo.repositories.ComicsRepository;
import ru.nasedkin.marvelapidemo.util.DataNotAddedException;
import ru.nasedkin.marvelapidemo.util.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;

@Service
public class CharacterService {

    private final CharacterRepository repository;
    private final ComicsRepository comicsRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public CharacterService(CharacterRepository characterRepository, ComicsRepository comicsRepository, ModelMapper modelMapper) {
        this.repository = characterRepository;
        this.comicsRepository = comicsRepository;
        this.modelMapper = modelMapper;
    }

    public List<CharacterDTO> findAll(PageRequest pageRequest) {
        Page<Character> characterPage = repository.findAll(pageRequest);
        List<Character> characterList = characterPage.getContent();
        if (characterList.isEmpty())
            throw new ResourceNotFoundException("Characters not found.");

        return characterList.stream().map(this::convertCharacterToDTO).toList();
    }

    public CharacterDTO findById(int id) {
        return convertCharacterToDTO(getCharacterFromDB(id));
    }

    public CharacterDTO findCharactersComics(int id) {
        Character character = getCharacterFromDB(id);

        if (character.getComics().isEmpty())
            throw new ResourceNotFoundException("Character with id " + "'" + id + "'" + " has 0 comics.");

        return convertCharacterToDTO(character);
    }

    public void save(Character character) {
        repository.save(character);
    }

    public void linkWithComics(int characterId, List<Integer> comicsId) {
        if (comicsId == null || comicsId.isEmpty())
            throw new DataNotAddedException("Nothing to link with.");

        Character character = getCharacterFromDB(characterId);
        for (int i : comicsId) {
            character.getComics().add(comicsRepository.findById(i).orElseThrow(() -> new ResourceNotFoundException("Comics with id: " + i + " not found.")));
        }
        repository.save(character);
    }

    public List<CharacterDTO> search(Specification<Character> spec, Pageable page) {
        return repository.findAll(spec, page).getContent().stream().map(this::convertCharacterToDTO).toList();
    }

    public void setThumbnail(int id, MultipartFile file) {
        Character character = getCharacterFromDB(id);
        try {
            character.setThumbnailName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
            character.setThumbnailType(file.getContentType());
            character.setThumbnail(file.getBytes());
            character.setThumbnailUri("http://localhost:8080/v1/public/characters/" + id + "/thumbnail");
            repository.save(character);
        } catch (Exception e) {
            throw new DataNotAddedException("Thumbnail not added.");
        }

    }

    public ResponseEntity<Resource> getThumbnail(int id) {
        Character character = getCharacterFromDB(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(character.getThumbnailType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "thumbnail; thumbnail name=\"" + character.getThumbnailName() + "\"")
                .body(new ByteArrayResource(character.getThumbnail()));
    }

    private Character getCharacterFromDB(int id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Character with id: " + id + " not found."));
    }

    private CharacterDTO convertCharacterToDTO(Character character) {
        return new CharacterDTO(character,
                character.getComics().stream()
                        .map(comics -> modelMapper.map(comics, ComicsDTO.class)).toList());
    }


//    public List<Character> findAll() {
//        List<Character> characters = repository.findAll();
//        if (characters.isEmpty())
//            throw new ResourceNotFoundException("Characters not found.");
//        for (Character character : characters) {
//            Set<Comics> comics = character.getComics();
//            for (Comics comics1 : comics) {
//                comics1.getCharacters().clear();
//            }
//        }
//        return characters;
//    }

    //    public List<Comics> findCharactersComics(int id) {
//        Character character = this.findById(id);
//        List<Comics> comics = new ArrayList<>(character.getComics());
//        if (comics.isEmpty())
//            throw new ResourceNotFoundException("Character with id " + "'" + id + "'" + " has 0 comics.");
//        return comics;
//    }
}

