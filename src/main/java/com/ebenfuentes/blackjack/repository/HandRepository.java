package com.ebenfuentes.blackjack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ebenfuentes.blackjack.model.Hand;

public interface HandRepository extends JpaRepository<Hand, Integer> {

}
