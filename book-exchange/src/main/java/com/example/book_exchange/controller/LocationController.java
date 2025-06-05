package com.example.book_exchange.controller;

import com.example.book_exchange.dto.LocationRequestDto;
import com.example.book_exchange.dto.LocationResponseDto;
import com.example.book_exchange.dto.TokenDto;
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
            @Valid @RequestBody LocationRequestDto dto) {
        authService.validateAdminToken(dto.getToken());
        return ResponseEntity.status(201)
                .body(convertToDto(locationService.createLocation(dto)));
    }

    @Operation(summary = "Update special location (only for admin)")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @PathVariable("id") Long id,
            @Valid @RequestBody LocationRequestDto dto) {
        ValidationUtils.validateId(id, "location");
        authService.validateAdminToken(dto.getToken());
        return ResponseEntity.ok(
                convertToDto(locationService.updateLocation(id, dto))
        );
    }

    @Operation(summary = "Delete special location (only for admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable("id") Long id,
            @Valid @RequestBody TokenDto tokenDto) {

        authService.validateAdminToken(tokenDto.getToken());
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