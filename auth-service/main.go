package main

import (
	"database/sql"
	"fmt"
	"log"
	"net/http"

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

	err = db.Ping()
	if err != nil {
		log.Fatalf("Failed to ping database: %v", err)
	}

	fmt.Println("Сonnected to PostgreSQL")

	authHandler := NewAuthHandler(config, db)

	router := mux.NewRouter()

	router.HandleFunc("/register", authHandler.RegisterHandler).Methods("POST")
	router.HandleFunc("/login", authHandler.LoginHandler).Methods("POST")
	router.HandleFunc("/validate", authHandler.ValidateHandler).Methods("POST")
	router.HandleFunc("/update", authHandler.UpdateHandler).Methods("POST")

	serverAddress := ":" + config.Port
	fmt.Printf("Auth service listening on %s\n", serverAddress)
	log.Fatal(http.ListenAndServe(serverAddress, router))
}
