#!/bin/bash

echo "Testando Microserviços"
echo "=================================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para testar endpoint
test_endpoint() {
    local url=$1
    local description=$2
    
    echo -e "\n${BLUE}Testando: $description${NC}"
    echo "URL: $url"
    
    response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$url")
    http_code="${response: -3}"
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}Sucesso (HTTP $http_code)${NC}"
        echo "Resposta:"
        cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json
    else
        echo -e "${RED}Erro (HTTP $http_code)${NC}"
        echo "Resposta:"
        cat /tmp/response.json
    fi
}

echo -e "${YELLOW}Aguardando serviços iniciarem...${NC}"
sleep 5

echo -e "\n${YELLOW}Verificando Eureka Dashboard...${NC}"
curl -s http://localhost:8761 > /dev/null && echo -e "${GREEN}Eureka OK${NC}" || echo -e "${RED}Eureka não disponível${NC}"

echo -e "\n${YELLOW}Testando Currency Service...${NC}"
test_endpoint "http://localhost:8100/currency/100/USD/BRL" "Cotação USD para BRL"
test_endpoint "http://localhost:8100/currency/50/EUR/BRL" "Cotação EUR para BRL"
test_endpoint "http://localhost:8100/currency/25/USD/BRL" "Cotação USD para BRL (cache)"

echo -e "\n${YELLOW}Testando Product Service...${NC}"
test_endpoint "http://localhost:8000/products/1/BRL" "Produto 1 em BRL"
test_endpoint "http://localhost:8000/products/2/USD" "Produto 2 em USD"
test_endpoint "http://localhost:8000/products/3/EUR" "Produto 3 em EUR"

echo -e "\n${YELLOW}Testando Greeting Service...${NC}"
test_endpoint "http://localhost:8200/greeting?name=João&language=en" "Saudação em Inglês"
test_endpoint "http://localhost:8200/greeting?name=Maria&language=es" "Saudação em Espanhol"
test_endpoint "http://localhost:8200/greeting?name=Pedro&language=it" "Saudação em Italiano"

echo -e "\n${YELLOW}Verificando Health Checks...${NC}"
test_endpoint "http://localhost:8100/actuator/health" "Currency Service Health"
test_endpoint "http://localhost:8000/actuator/health" "Product Service Health"
test_endpoint "http://localhost:8200/actuator/health" "Greeting Service Health"

echo -e "\n${GREEN}Testes concluídos!${NC}"
echo -e "${BLUE}Acesse http://localhost:8761 para ver o Eureka Dashboard${NC}"
