package me.nyaruko166.nyarukotoolkitweb.repository;

import me.nyaruko166.nyarukotoolkitweb.model.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Integer> {
}
