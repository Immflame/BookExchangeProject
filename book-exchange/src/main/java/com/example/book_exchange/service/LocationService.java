package com.example.book_exchange.service;

import com.example.book_exchange.dto.LocationRequestDto;
import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public Location createLocation(LocationRequestDto dto) {
        return locationRepository.save(
                Location.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build()
        );
    }

    public Location updateLocation(Long id, LocationRequestDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        location.setName(dto.getName());
        location.setDescription(dto.getDescription());
        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }
}
