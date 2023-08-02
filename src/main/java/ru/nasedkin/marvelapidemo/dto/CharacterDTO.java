package ru.nasedkin.marvelapidemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.nasedkin.marvelapidemo.models.Character;

import java.util.List;

@Data
@AllArgsConstructor
public class CharacterDTO {

    private Character character;
    private List<ComicsDTO> comicsList;

}
