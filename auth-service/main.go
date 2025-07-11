package main

import (
	"context"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
)

var db *sql.DB

func main() {
	config := LoadConfig()

	var err error
	db, err = sql.Open("postgres", config.DatabaseURL)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()

	//проверка соединения с бд
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := db.PingContext(ctx); err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	fmt.Println("Connected to PostgreSQL")
	//окончание проверки

	authHandler := NewAuthHandler(config, db)
	router := mux.NewRouter()

	router.HandleFunc("/register", authHandler.RegisterHandler).Methods("POST")
	router.HandleFunc("/login", authHandler.LoginHandler).Methods("POST")
	router.HandleFunc("/validate", authHandler.ValidateHandler).Methods("GET")
	router.HandleFunc("/update", authHandler.UpdateHandler).Methods("PUT")

	serverAddress := ":" + config.Port
	fmt.Printf("Auth service listening on %s\n", serverAddress)
	log.Fatal(http.ListenAndServe(serverAddress, router))
}
