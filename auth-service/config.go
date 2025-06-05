package main

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	Port        string
	SecretKey   string
	DatabaseURL string
}

func LoadConfig() Config {
	err := godotenv.Load()
	if err != nil {
		log.Println("Error loading .env file")
	}

	port := os.Getenv("AUTH_SERVICE_PORT")
	secretKey := os.Getenv("AUTH_SERVICE_SECRET_KEY")
	databaseURL := os.Getenv("AUTH_SERVICE_DATABASE_URL")

	return Config{
		Port:        port,
		SecretKey:   secretKey,
		DatabaseURL: databaseURL,
	}
}
