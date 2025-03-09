package com.ebenfuentes.blackjack.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ebenfuentes.blackjack.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

}
