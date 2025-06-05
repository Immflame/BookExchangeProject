package com.example.book_exchange.service;

import com.example.book_exchange.dto.LocationRequestDto;
import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private Location location;
    private LocationRequestDto locationRequestDto;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1L);
        location.setName("Test Location");
        location.setDescription("Test Description");

        locationRequestDto = new LocationRequestDto();
        locationRequestDto.setName("Updated Location");
        locationRequestDto.setDescription("Updated Description");
    }

    @Test
    void getAllLocations_ShouldReturnAllLocations() {
        when(locationRepository.findAll()).thenReturn(List.of(location));
        List<Location> result = locationService.getAllLocations();
        assertEquals(1, result.size());
        assertEquals("Test Location", result.get(0).getName());
    }

    @Test
    void getLocationById_ShouldReturnLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        Optional<Location> result = locationService.getLocationById(1L);
        assertTrue(result.isPresent());
        assertEquals("Test Location", result.get().getName());
    }

    @Test
    void getLocationById_NotFound_ShouldReturnEmpty() {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Location> result = locationService.getLocationById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void createLocation_ShouldReturnSavedLocation() {
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        Location result = locationService.createLocation(locationRequestDto);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Location", result.getName());
    }

    @Test
    void updateLocation_ShouldUpdateExistingLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        Location result = locationService.updateLocation(1L, locationRequestDto);
        assertEquals("Updated Location", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void updateLocation_NotFound_ShouldThrowException() {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> locationService.updateLocation(1L, locationRequestDto));
    }

    @Test
    void deleteLocation_ShouldDeleteExistingLocation() {
        when(locationRepository.existsById(1L)).thenReturn(true);

        locationService.deleteLocation(1L);

        verify(locationRepository).existsById(1L);
        verify(locationRepository).deleteById(1L);
    }

    @Test
    void deleteLocation_NotFound_ShouldThrowException() {
        when(locationRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> locationService.deleteLocation(1L));

        verify(locationRepository).existsById(1L);
        verify(locationRepository, never()).deleteById(anyLong());
    }
}