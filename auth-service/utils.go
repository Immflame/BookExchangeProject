package main

import (
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

func IsValidCredentials(username string, password string) bool {
	if strings.TrimSpace(username) == "" || strings.TrimSpace(password) == "" || strings.Contains(username, " ") || strings.Contains(password, " ") {
		return false
	}

	return true
}

func GenerateToken(userID int, username string, role string, secretKey string) (string, error) {
	claims := jwt.MapClaims{
		"user_id":  userID,
		"username": username,
		"role":     role,
		"exp":      time.Now().Add(time.Minute * 5).Unix(), // время жизни 5 минут
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
		log.Printf("VerifyToken: Error parsing token: %v", err)
		return 0, "", "", err
	}

	if !token.Valid {
		log.Printf("VerifyToken: Token is not valid")
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
