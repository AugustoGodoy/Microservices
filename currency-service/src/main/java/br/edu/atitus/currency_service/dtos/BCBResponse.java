package br.edu.atitus.currency_service.dtos;

import java.util.List;

public class BCBResponse {
    private List<BCBValue> value;
    
    public List<BCBValue> getValue() {
        return value;
    }
    
    public void setValue(List<BCBValue> value) {
        this.value = value;
    }
    
    public static class BCBValue {
        private String cotacaoCompra;
        private String cotacaoVenda;
        private String dataHoraCotacao;
        
        public String getCotacaoCompra() {
            return cotacaoCompra;
        }
        
        public void setCotacaoCompra(String cotacaoCompra) {
            this.cotacaoCompra = cotacaoCompra;
        }
        
        public String getCotacaoVenda() {
            return cotacaoVenda;
        }
        
        public void setCotacaoVenda(String cotacaoVenda) {
            this.cotacaoVenda = cotacaoVenda;
        }
        
        public String getDataHoraCotacao() {
            return dataHoraCotacao;
        }
        
        public void setDataHoraCotacao(String dataHoraCotacao) {
            this.dataHoraCotacao = dataHoraCotacao;
        }
    }
}
