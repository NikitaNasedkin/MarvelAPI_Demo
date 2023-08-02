package ru.nasedkin.marvelapidemo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "characters")
@ToString(exclude = "characters")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comics")
public class Comics {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "comics", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<Character> characters;

    @Column(name = "thumbnail_uri")
    String thumbnailUri;

    @JsonIgnore
    @Column(name = "thumbnail")
    private byte[] thumbnail;
    @JsonIgnore
    @Column(name = "thumbnail_name")
    private String thumbnailName;
    @JsonIgnore
    @Column(name = "thumbnail_type")
    private String thumbnailType;
}
