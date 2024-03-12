#!/bin/sh
var=$(curl -d '{"name":"coole Gruppe","personen":["clachi2","Mariesh1710","simonMkraemer","Doguhan58"]}' -H "Content-Type: application/json" -X POST http://localhost:9000/api/gruppen)
curl -d '{"grund":"Kino","glaeubiger":"Doguhan58","cent":10000,"schuldner":["Mariesh1710","clachi2","simonMkraemer"]}' -H "Content-Type: application/json" -X POST http://localhost:9000/api/gruppen/$var/auslagen
curl -d '{"grund":"Doener","glaeubiger":"clachi2","cent":2000,"schuldner":["clachi2","simonMkraemer"]}' -H "Content-Type: application/json" -X POST http://localhost:9000/api/gruppen/$var/auslagen
curl -d '{"grund":"Hotel","glaeubiger":"simonMkraemer","cent":4000,"schuldner":["Mariesh1710","Doguhan58"]}' -H "Content-Type: application/json" -X POST http://localhost:9000/api/gruppen/$var/auslagen
curl -d '{"grund":"Essen","glaeubiger":"Mariesh1710","cent":5000,"schuldner":["clachi2","simonMkraemer","Doguhan58"]}' -H "Content-Type: application/json" -X POST http://localhost:9000/api/gruppen/$var/auslagen
