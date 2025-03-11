package com.ebenfuentes.blackjack.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.ebenfuentes.blackjack.model.Player;
import com.ebenfuentes.blackjack.service.GameService;

@CrossOrigin(origins = "http://localhost:5173")
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

    // Player places a bet
    @PostMapping("/{id}/bet")
    public void placeBet(@PathVariable int id, @RequestBody Map<String, Integer> request) {
        int betAmount = request.get("amount");
        gameService.placeBet(id, betAmount);
    }

    // Get player status
    @GetMapping("/{id}")
    public Map<String, Object> getPlayerStatus(@PathVariable int id) {
        return gameService.checkGameStatus(id);
    }

    // Start a game for a player
    @PostMapping("/{id}/start")
    public Map<String, Object> startGame(@PathVariable int id) {
        return gameService.startGame(id);
    }


    // Player hits (gets a new card)
    @PostMapping("/{id}/hit")
    public Map<String, Object> hit(@PathVariable int id) {
        return gameService.hit(id); 
    }
    
    // Get current hand value for a player
    @GetMapping("/{id}/hand-value")
    public Map<String, Object> getPlayerHandDetails(@PathVariable int id) {
        return gameService.getPlayerHandDetails(id);
    }
    
    // Player stands (dealer plays)
    @PostMapping("/{id}/stand")
    public Map<String, Object> stand(@PathVariable int id) {
        return gameService.stand(id);
    }

    // Player doubles down
    @PostMapping("/{id}/double-down")
    public Map<String, Object> doubleDown(@PathVariable int id) {
        return gameService.doubleDown(id);
    }

    // Player splits
    @PostMapping("/{id}/split")
    public void split(@PathVariable int id) {
        gameService.split(id);
    }

    // Reset game for a player
    @PostMapping("/{id}/reset")
    public void resetGame(@PathVariable int id) {
        gameService.resetGame(id);
    }
    
    // Get player balance
    @GetMapping("/{id}/balance")
    public Map<String, Object> getPlayerBalance(@PathVariable int id) {
        return gameService.getPlayerBalance(id);
    }

    
}
