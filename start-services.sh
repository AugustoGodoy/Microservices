#!/bin/bash

echo "Iniciando Microserviços..."
echo "=============================="

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para iniciar serviço
start_service() {
    local service_name=$1
    local port=$2
    local directory=$3
    
    echo -e "\n${BLUE}Iniciando $service_name na porta $port...${NC}"
    cd "$directory"
    
    nohup ./mvnw spring-boot:run > "../logs/$service_name.log" 2>&1 &
    local pid=$!
    echo "$pid" > "../logs/$service_name.pid"
    
    echo -e "${GREEN}$service_name iniciado (PID: $pid)${NC}"
    echo "Log: microservices/logs/$service_name.log"
}

mkdir -p logs

echo -e "${YELLOW}Criando diretório de logs...${NC}"

# 1. Config Service (porta 8888)
start_service "config-service" "8888" "config-service"

echo -e "${YELLOW}Aguardando Config Service inicializar...${NC}"
sleep 10

# 2. Discovery Service (porta 8761)
start_service "discovery-service" "8761" "discovery-service"

echo -e "${YELLOW}Aguardando Discovery Service inicializar...${NC}"
sleep 15

# 3. Currency Service (porta 8100)
start_service "currency-service" "8100" "currency-service"

echo -e "${YELLOW}Aguardando Currency Service inicializar...${NC}"
sleep 15

# 4. Product Service (porta 8000)
start_service "product-service" "8000" "product-service"

echo -e "${YELLOW}Aguardando Product Service inicializar...${NC}"
sleep 15

# 5. Greeting Service (porta 8200)
start_service "greeting-service" "8200" "greeting-service"

echo -e "\n${GREEN}Todos os serviços foram iniciados!${NC}"
echo -e "${BLUE}Verificar logs em: microservices/logs/${NC}"
echo -e "${BLUE}Eureka Dashboard: http://localhost:8761${NC}"
echo -e "${BLUE}Executar testes: ./test-api.sh${NC}"

echo -e "\n${YELLOW}PIDs dos serviços:${NC}"
for service in config-service discovery-service currency-service product-service greeting-service; do
    if [ -f "logs/$service.pid" ]; then
        local pid=$(cat "logs/$service.pid")
        echo "  $service: $pid"
    fi
done
