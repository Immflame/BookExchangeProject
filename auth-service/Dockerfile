FROM golang:1.23-alpine AS build
WORKDIR /app

COPY go.mod go.sum .env ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o auth-service

FROM alpine:latest
WORKDIR /app

COPY --from=build /app/auth-service .

EXPOSE 8081
CMD ["./auth-service"]