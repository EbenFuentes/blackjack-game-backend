package com.ebenfuentes.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.ebenfuentes.model.Player;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

}
