package main

import (
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

func ExtractToken(r *http.Request) (string, error) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		return "", fmt.Errorf("missing Authorization header")
	}

	parts := strings.Split(authHeader, " ")
	if len(parts) != 2 || strings.ToLower(parts[0]) != "bearer" {
		return "", fmt.Errorf("invalid Authorization header format")
	}

	return parts[1], nil
}

func IsValidCredentials(username string, password string) bool {
	if username == "" || password == "" ||
		strings.Contains(username, " ") || strings.Contains(password, " ") ||
		len(username) < 4 || len(password) < 4 {
		return false
	}

	return true
}

func GenerateToken(userID int, username string, role string, secretKey string) (string, error) {
	claims := jwt.MapClaims{
		"user_id":  userID,
		"username": username,
		"role":     role,
		"exp":      time.Now().Add(time.Minute * 15).Unix(), // время жизни 5 минут
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	tokenString, err := token.SignedString([]byte(secretKey))
	if err != nil {
		return "", err
	}

	return tokenString, nil
}

func HashPassword(password string) (string, error) {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return "", err
	}
	return string(hashedPassword), nil
}

func VerifyToken(tokenString string, secretKey string) (int, string, string, error) {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secretKey), nil
	})

	if err != nil {
		return 0, "", "", err
	}

	if !token.Valid {
		return 0, "", "", fmt.Errorf("invalid token")
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok {
		userIDFloat, ok := claims["user_id"].(float64)
		if !ok {
			return 0, "", "", fmt.Errorf("invalid user_id in token")
		}
		userID := int(userIDFloat)

		username, ok := claims["username"].(string)
		if !ok {
			return 0, "", "", fmt.Errorf("invalid username in token")
		}

		role, ok := claims["role"].(string)
		if !ok {
			return 0, "", "", fmt.Errorf("invalid role in token")
		}

		return userID, username, role, nil
	}

	return 0, "", "", fmt.Errorf("invalid token")
}
