package ru.nasedkin.marvelapidemo.controllers;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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
import ru.nasedkin.marvelapidemo.models.Character;
import ru.nasedkin.marvelapidemo.models.Comics;
import ru.nasedkin.marvelapidemo.srevices.ComicsService;
import ru.nasedkin.marvelapidemo.util.DataNotAddedException;
import ru.nasedkin.marvelapidemo.util.ErrorResponse;
import ru.nasedkin.marvelapidemo.util.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("v1/public/comics")
public class ComicsController {

    private final ComicsService service;

    @Autowired
    public ComicsController(ComicsService comics) {
        this.service = comics;
    }

    @GetMapping
    public List<Comics> findAll(@RequestParam(required = false, defaultValue = "0") int page,
                                @RequestParam(required = false, defaultValue = "10") int size,
                                @RequestParam(required = false, defaultValue = "id") String sortBy) {
        return service.findAll(PageRequest.of(page, size).withSort(Sort.by(sortBy)));
    }

    @GetMapping(value = "/search")
    public Page<Comics> search(@Filter Specification<Comics> spec, Pageable page) {
        return service.search(spec, page);
    }

    @GetMapping("/{id}")
    public Comics getById(@PathVariable int id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/characters")
    public Set<Character> getComicsCharacters(@PathVariable int id) {
        return service.findComicsCharacters(id);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid Comics comics,
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

        service.save(comics);

        return new ResponseEntity<>(comics.getName() + " successfully added!", HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/link")
    public ResponseEntity<?> linkWithCharacters(@PathVariable int id, @RequestBody ArrayList<Integer> charactersId) {
        service.linkWithCharacters(id, charactersId);
        return new ResponseEntity<>("Comics with id: " + id + " successfully linked with character " + charactersId.toString(), HttpStatus.ACCEPTED);
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
}
