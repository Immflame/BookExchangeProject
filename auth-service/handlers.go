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

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if !IsValid(req.Username) {
		http.Error(w, "Invalid username", http.StatusBadRequest)
		return
	}

	if !IsValid(req.Password) {
		http.Error(w, "Invalid password", http.StatusBadRequest)
		return
	}

	//проверяем существует ли пользователь в бд
	query := `SELECT COUNT(*) FROM users WHERE username = $1`
	var n int

	if err := h.DB.QueryRow(query, req.Username).Scan(&n); err != nil {
		http.Error(w, "Failed to register user", http.StatusInternalServerError)
		return
	}

	if n != 0 {
		http.Error(w, "A user with this username already exists", http.StatusConflict)
		return
	}
	// окончание проверки

	hashedPassword, err := HashPassword(req.Password)
	if err != nil {
		http.Error(w, "Failed to hash password", http.StatusInternalServerError)
		return
	}

	var userID int
	query = `
    INSERT INTO users (username, password, role)
    VALUES ($1, $2, 'user')
    RETURNING id
    `

	if err = h.DB.QueryRow(query, req.Username, hashedPassword).Scan(&userID); err != nil {
		http.Error(w, "Failed to register user", http.StatusInternalServerError)
		return
	}

	token, err := GenerateToken(userID, req.Username, "user", h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}
	/// изменить ниже
	resp := RegisterResponse{Token: token}
	w.Header().Set("Content-Type", "application/json")
	if err = json.NewEncoder(w).Encode(resp); err != nil {
		http.Error(w, "Failed to encode", http.StatusInternalServerError)
		return
	}
}

func (h *AuthHandler) LoginHandler(w http.ResponseWriter, r *http.Request) {

	var req AuthRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	var user User
	query := `
    SELECT id, username, password, role
    FROM users
    WHERE username = $1
    `

	if err := h.DB.QueryRow(query, req.Username).Scan(&user.ID, &user.Username, &user.Password, &user.Role); err != nil {
		if err == sql.ErrNoRows {
			http.Error(w, "Invalid credentials", http.StatusUnauthorized)
			return
		}
		http.Error(w, "Failed to retrieve user", http.StatusInternalServerError)
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
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
	token, err := ExtractToken(r)
	if err != nil {
		http.Error(w, err.Error(), http.StatusUnauthorized)
		return
	}

	userID, username, role, err := VerifyToken(token, h.Config.SecretKey)
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
	json.NewEncoder(w).Encode(resp)
}

func (h *AuthHandler) UpdateHandler(w http.ResponseWriter, r *http.Request) {

	token, err := ExtractToken(r)
	if err != nil {
		http.Error(w, err.Error(), http.StatusUnauthorized)
		return
	}

	userID, username, role, err := VerifyToken(token, h.Config.SecretKey)
	if err != nil {
		http.Error(w, "Invalid token", http.StatusUnauthorized)
		return
	}

	var req UpdateRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if req.Username != "" {
		if !IsValid(req.Username) {
			http.Error(w, "Invalid username", http.StatusBadRequest)
		}

		query := `SELECT COUNT(*) FROM users WHERE username = $1`
		var n int

		if err := h.DB.QueryRow(query, req.Username).Scan(&n); err != nil {
			http.Error(w, "Failed to register user", http.StatusInternalServerError)
			return
		}

		if n != 0 {
			http.Error(w, "A user with this username already exists", http.StatusConflict)
			return
		}

		query = `UPDATE users
    	SET username = $1 
    	WHERE id = $2`

		_, err := h.DB.Exec(query, req.Username, userID)

		if err != nil {
			http.Error(w, "Failed update username", http.StatusInternalServerError)
		}

		username = req.Username
	}

	if req.Password != "" {
		if !IsValid(req.Password) {
			http.Error(w, "Invalid password", http.StatusBadRequest)
		}

		query := `UPDATE users
    	SET password = $1 
    	WHERE id = $2`

		hashedPassword, err := HashPassword(req.Password)
		if err != nil {
			http.Error(w, "Failed to hash password", http.StatusInternalServerError)
		}

		_, err = h.DB.Exec(query, hashedPassword, userID)

		if err != nil {
			http.Error(w, "Failed update password", http.StatusInternalServerError)
		}
	}

	newToken, err := GenerateToken(userID, username, role, h.Config.SecretKey)
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
