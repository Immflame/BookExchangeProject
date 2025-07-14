package com.example.book_exchange.controller;

import com.example.book_exchange.dto.LocationRequestDto;
import com.example.book_exchange.dto.LocationResponseDto;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.service.LocationService;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Location Controller", description = "Operations with special locations")
@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired private LocationService locationService;
    @Autowired private AuthService authService;

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new InvalidTokenException("Invalid authorization header");
    }

    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations().stream()
                        .map(this::convertToDto)
                        .toList());
    }

    @Operation(summary = "Get special location by location_id")
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(
            @PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "location");
        return ResponseEntity.ok(locationService.getLocationById(id)
                        .map(this::convertToDto)
                        .orElseThrow(() -> new ResourceNotFoundException("Location not found")));
    }

    @Operation(summary = "Create new special location (only for admin)")
    @PostMapping
    public ResponseEntity<LocationResponseDto> createLocation(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LocationRequestDto dto) {
        String token = extractToken(authHeader);
        authService.validateAdminToken(token);
        return ResponseEntity.status(201)
                .body(convertToDto(locationService.createLocation(dto)));
    }

    @Operation(summary = "Update special location (only for admin)")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long id,
            @Valid @RequestBody LocationRequestDto dto) {
        String token = extractToken(authHeader);
        ValidationUtils.validateId(id, "location");
        authService.validateAdminToken(token);
        return ResponseEntity.ok(
                convertToDto(locationService.updateLocation(id, dto))
        );
    }

    @Operation(summary = "Delete special location (only for admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long id) {
        String token = extractToken(authHeader);
        authService.validateAdminToken(token);
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    private LocationResponseDto convertToDto(Location location) {
        return new LocationResponseDto(
                location.getId(),
                location.getName(),
                location.getDescription()
        );
    }
}