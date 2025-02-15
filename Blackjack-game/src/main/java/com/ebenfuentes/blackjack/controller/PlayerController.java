package com.ebenfuentes.blackjack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebenfuentes.blackjack.model.Player;
import com.ebenfuentes.blackjack.service.GameService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
	
    private final GameService gameService;

    public PlayerController(GameService gameService) {
        this.gameService = gameService;
    }

    // Create a player
    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return gameService.createPlayer(player.getUsername(), player.getBalance());
    }

    // Get player status
    @GetMapping("/{id}")
    public String getPlayerStatus(@PathVariable int id) {
        return gameService.checkGameStatus(id);
    }

    // Start a game for a player
    @PostMapping("/{id}/start")
    public void startGame(@PathVariable int id) {
        gameService.startGame(id);
    }

    // Player hits (gets a new card)
    @PostMapping("/{id}/hit")
    public void hit(@PathVariable int id) {
        gameService.hit(id);
    }

    // Reset game for a player
    @PostMapping("/{id}/reset")
    public void resetGame(@PathVariable int id) {
        gameService.resetGame(id);
    }
}
