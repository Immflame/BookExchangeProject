package main

import (
	"database/sql"
	"encoding/json"
	"net/http"

	"golang.org/x/crypto/bcrypt"
)

type AuthHandler struct {
	Config Config
	DB     *sql.DB
}

func NewAuthHandler(config Config, db *sql.DB) *AuthHandler {
	return &AuthHandler{Config: config, DB: db}
}

func (h *AuthHandler) RegisterHandler(w http.ResponseWriter, r *http.Request) {

	var req RegisterRequest
	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if !IsValidCredentials(req.Username, req.Password) {
		http.Error(w, "Invalid username or password", http.StatusBadRequest)
		return
	}

	//проверяем существует ли пользователь в бд
	query := `SELECT COUNT(*) FROM users WHERE username = $1`
	var n int = 0
	err = h.DB.QueryRow(query, req.Username).Scan(&n)
	if err != nil {
		http.Error(w, "Failed to register user", http.StatusInternalServerError)
		return
	}

	if n != 0 {
		http.Error(w, "A user with this username already exists", http.StatusConflict)
		return
	}
	// окончание проверки

	hashedPassword, err := HashPassword(req.Password)

	var userID int64
	query = `
    INSERT INTO users (username, password, role)
    VALUES ($1, $2, 'user')
    RETURNING id
    `

	err = h.DB.QueryRow(query, req.Username, hashedPassword).Scan(&userID)
	if err != nil {
		http.Error(w, "Failed to register user", http.StatusInternalServerError)
		return
	}

	token, err := GenerateToken(int(userID), req.Username, "user", h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}

	resp := RegisterResponse{Token: token}
	w.Header().Set("Content-Type", "application/json")
	if err = json.NewEncoder(w).Encode(resp); err != nil {
		http.Error(w, "Failed to encode", http.StatusInternalServerError)
		return
	}
}

func (h *AuthHandler) LoginHandler(w http.ResponseWriter, r *http.Request) {

	var req AuthRequest
	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	var user User
	query := `
    SELECT id, username, password, role
    FROM users
    WHERE username = $1
    `
	err = h.DB.QueryRow(query, req.Username).Scan(&user.ID, &user.Username, &user.Password, &user.Role)
	if err != nil {
		if err == sql.ErrNoRows {
			http.Error(w, "Invalid credentials", http.StatusUnauthorized)
			return
		}
		http.Error(w, "Failed to retrieve user", http.StatusInternalServerError)
		return
	}

	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password))

	if err != nil {
		if err == bcrypt.ErrMismatchedHashAndPassword {
			http.Error(w, "Invalid credentials", http.StatusUnauthorized)
			return
		}
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}

	token, err := GenerateToken(int(user.ID), user.Username, user.Role, h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}

	resp := AuthResponse{Token: token}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	if err = json.NewEncoder(w).Encode(resp); err != nil {
		http.Error(w, "Failed to encode", http.StatusInternalServerError)
		return
	}
}

func (h *AuthHandler) ValidateHandler(w http.ResponseWriter, r *http.Request) {

	var req ValidateRequest
	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if req.Token == "" {
		http.Error(w, "Missing token", http.StatusUnauthorized)
		return
	}

	userID, username, role, err := VerifyToken(req.Token, h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Invalid token", http.StatusUnauthorized)
		return
	}

	resp := ValidateResponse{
		UserID:   userID,
		Username: username,
		Role:     role,
		Valid:    true,
	}
	w.Header().Set("Content-Type", "application/json")
	if err = json.NewEncoder(w).Encode(resp); err != nil {
		http.Error(w, "Failed to encode", http.StatusInternalServerError)
		return
	}
}

func (h *AuthHandler) UpdateHandler(w http.ResponseWriter, r *http.Request) {

	var req UpdateRequest
	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if !IsValidCredentials(req.Username, req.Password) {
		http.Error(w, "Invalid sername or password", http.StatusBadRequest)
		return
	}

	//проверяем существует ли пользователь c таким же username в бд
	query := `SELECT COUNT(*) FROM users WHERE username = $1`
	var n int = 0
	err = h.DB.QueryRow(query, req.Username).Scan(&n)
	if err != nil {
		http.Error(w, "Failed to register user", http.StatusInternalServerError)
		return
	}

	if n != 0 {
		http.Error(w, "A user with this username already exists", http.StatusConflict)
		return
	}
	// окончание проверки

	userID, _, role, err := VerifyToken(req.Token, h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Invalid token", http.StatusUnauthorized)
		return
	}

	hashedPassword, err := HashPassword(req.Password)
	if err != nil {
		http.Error(w, "Failed to hash password", http.StatusInternalServerError)
		return
	}

	query = `
    UPDATE users 
    SET username = $1, password = $2 
    WHERE id = $3
    `
	_, err = h.DB.Exec(query, req.Username, hashedPassword, userID)
	if err != nil {
		http.Error(w, "Failed to update user", http.StatusInternalServerError)
		return
	}

	newToken, err := GenerateToken(userID, req.Username, role, h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}

	resp := UpdateResponse{Token: newToken}
	w.Header().Set("Content-Type", "application/json")
	if err = json.NewEncoder(w).Encode(resp); err != nil {
		http.Error(w, "Failed to encode", http.StatusInternalServerError)
		return
	}
}
