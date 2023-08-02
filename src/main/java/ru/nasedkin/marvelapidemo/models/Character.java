package ru.nasedkin.marvelapidemo.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "comics")
@ToString(exclude = "comics")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "character")
public class Character {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "character_comics",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "comics_id")
    )
    @JsonBackReference
    private Set<Comics> comics;

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
