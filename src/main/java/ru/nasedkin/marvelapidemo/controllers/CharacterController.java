package ru.nasedkin.marvelapidemo.controllers;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nasedkin.marvelapidemo.dto.CharacterDTO;
import ru.nasedkin.marvelapidemo.models.Character;
import ru.nasedkin.marvelapidemo.srevices.CharacterService;
import ru.nasedkin.marvelapidemo.util.DataNotAddedException;
import ru.nasedkin.marvelapidemo.util.ErrorResponse;
import ru.nasedkin.marvelapidemo.util.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("v1/public/characters")
public class CharacterController {

    private final CharacterService service;

    @Autowired
    public CharacterController(CharacterService service) {
        this.service = service;
    }

    @GetMapping
    public List<CharacterDTO> findAll(@RequestParam(required = false, defaultValue = "0") int page,
                                      @RequestParam(required = false, defaultValue = "10") int size,
                                      @RequestParam(required = false, defaultValue = "id") String sortBy) {
        return service.findAll(PageRequest.of(page, size).withSort(Sort.by(sortBy)));
    }

    @GetMapping(value = "/search")
    public List<CharacterDTO> search(@Filter Specification<Character> spec, Pageable page) {
        return service.search(spec, page);
    }

    @GetMapping("/{id}")
    public CharacterDTO getById(@PathVariable int id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/comics")
    public CharacterDTO getCharactersComics(@PathVariable int id) {

        return service.findCharactersComics(id);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid Character character,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessage.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append(";");
            }
            throw new DataNotAddedException(errorMessage.toString());
        }

        service.save(character);

        return new ResponseEntity<>(character.getName() + " successfully added!", HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/link")
    public ResponseEntity<?> linkWithComics(@PathVariable int id, @RequestBody ArrayList<Integer> comicsIds) {
        service.linkWithComics(id, comicsIds);
        return new ResponseEntity<>("Character with id: " + id + " successfully linked with comics " + comicsIds.toString(), HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/thumbnail/set")
    public ResponseEntity<?> setThumbnail(@PathVariable int id,
                                          @RequestParam("file") MultipartFile file) {
        service.setThumbnail(id, file);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable int id) {
        return service.getThumbnail(id);
    }


    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(ResourceNotFoundException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(DataNotAddedException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(DataIntegrityViolationException e) {
        return new ResponseEntity<>(new ErrorResponse("Not added. " + "Character" + " already exists.", LocalDateTime.now()),
                HttpStatus.BAD_REQUEST);
    }

}
