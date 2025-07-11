package main

type User struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	Password string `json:"password"`
	Role     string `json:"-"`
}

type AuthRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

type RegisterRequest AuthRequest

type UpdateRequest AuthRequest

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
