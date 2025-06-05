package com.example.book_exchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import com.example.book_exchange.dto.LocationRequestDto;
import com.example.book_exchange.dto.LocationResponseDto;
import com.example.book_exchange.dto.TokenDto;
import com.example.book_exchange.exception.GlobalExceptionHandler;
import com.example.book_exchange.exception.InvalidTokenException;
import com.example.book_exchange.exception.PermissionDeniedException;
import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Location;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private LocationService locationService;
    @Mock private AuthService authService;

    @InjectMocks private LocationController locationController;

    private Location testLocation;
    private LocationRequestDto testLocationDto;
    private LocationResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testLocation = new Location(1L, "Library", "Main library");
        testLocationDto = new LocationRequestDto();
        testLocationDto.setToken("admin-token");
        testLocationDto.setName("Library");
        testLocationDto.setDescription("Main library");

        testResponseDto = new LocationResponseDto(1L, "Library", "Main library");
    }

    @Test
    void getAllLocations_Success() throws Exception {
        when(locationService.getAllLocations()).thenReturn(Collections.singletonList(testLocation));

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Library"));
    }

    @Test
    void getLocationById_Success() throws Exception {
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));

        mockMvc.perform(get("/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Library"));
    }

    @Test
    void getLocationById_NotFound() throws Exception {
        when(locationService.getLocationById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/locations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"));
    }

    @Test
    void getLocationById_InvalidId() throws Exception {
        mockMvc.perform(get("/locations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid location ID"));
    }

    @Test
    void createLocation_Success() throws Exception {
        when(locationService.createLocation(any(LocationRequestDto.class))).thenReturn(testLocation);

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLocationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Library"));
    }

    @Test
    void createLocation_InvalidData() throws Exception {
        LocationRequestDto invalidDto = new LocationRequestDto();
        invalidDto.setToken("admin-token");
        invalidDto.setName(""); // Пустое имя

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Name is required")));
    }

    @Test
    void createLocation_InvalidToken() throws Exception {
        // Используем doThrow для void метода
        doThrow(new InvalidTokenException("Invalid token"))
                .when(authService).validateAdminToken("invalid-token");

        LocationRequestDto invalidTokenDto = new LocationRequestDto();
        invalidTokenDto.setToken("invalid-token");
        invalidTokenDto.setName("Library");

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTokenDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void createLocation_NotAdmin() throws Exception {
        // Используем doThrow для void метода
        doThrow(new PermissionDeniedException("Admin role required"))
                .when(authService).validateAdminToken("user-token");

        LocationRequestDto userDto = new LocationRequestDto();
        userDto.setToken("user-token");
        userDto.setName("Library");

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role required"));
    }

    @Test
    void updateLocation_Success() throws Exception {
        when(locationService.updateLocation(anyLong(), any(LocationRequestDto.class))).thenReturn(testLocation);

        mockMvc.perform(put("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLocationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Library"));
    }

    @Test
    void updateLocation_NotFound() throws Exception {
        when(locationService.updateLocation(anyLong(), any(LocationRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Location not found"));

        mockMvc.perform(put("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLocationDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"));
    }

    @Test
    void updateLocation_InvalidId() throws Exception {
        mockMvc.perform(put("/locations/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLocationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid location ID"));
    }


    @Test
    void deleteLocation_Success() throws Exception {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("admin-token");

        mockMvc.perform(delete("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isNoContent());

        verify(locationService).deleteLocation(1L);
    }

    @Test
    void deleteLocation_NotFound() throws Exception {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("admin-token");

        doThrow(new ResourceNotFoundException("Location not found"))
                .when(locationService).deleteLocation(1L);

        mockMvc.perform(delete("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"));
    }


    @Test
    void deleteLocation_InvalidToken() throws Exception {
        doThrow(new InvalidTokenException("Invalid token"))
                .when(authService).validateAdminToken("invalid-token");

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("invalid-token");

        mockMvc.perform(delete("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void deleteLocation_NotAdmin() throws Exception {
        doThrow(new PermissionDeniedException("Admin role required"))
                .when(authService).validateAdminToken("user-token");

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("user-token");

        mockMvc.perform(delete("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role required"));
    }
}