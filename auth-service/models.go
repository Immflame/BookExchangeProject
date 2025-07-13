package main

type User struct {
	ID       int64
	Username string
	Password string
	Role     string
}

type AuthRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

type RegisterRequest AuthRequest

type UpdateRequest struct {
	Username string `json:"username,omitempty"`
	Password string `json:"password,omitempty"`
}

type AuthResponse struct {
	Token string `json:"token"`
}

type RegisterResponse AuthResponse

type UpdateResponse AuthResponse

type ValidateResponse struct {
	UserID   int    `json:"user_id"`
	Username string `json:"username"`
	Role     string `json:"role"`
	Valid    bool   `json:"valid"`
}
