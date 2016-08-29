package br.com.quintoandar.consultasbr.iptusp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RespostaConsultaDebito {

	private String codigoContribuinte;
	
	private Boolean possuiDebitos = Boolean.FALSE;

	private Integer anoExercicioAtual = null;
	
	private InformacaoDebito debitoExercicioAtual = null;
	
	private Map<Integer, InformacaoDebito> debitosAnosAnteriores = new HashMap<Integer, InformacaoDebito>();
	
	public RespostaConsultaDebito(String codContribuinte, Integer exercicioAtual){
		this.codigoContribuinte = codContribuinte;
		this.setAnoExercicioAtual(exercicioAtual);
	}
	
	public Boolean getPossuiDebitos(){
		return this.possuiDebitos;
	}
	
	public void sedtPossuiDebitos(Boolean possuiDebitos){
		this.possuiDebitos = possuiDebitos;
	}
	
	public void addInformacaoDebito(InformacaoDebito infoDebito){
		debitosAnosAnteriores.put(infoDebito.getAnoExercicio(), infoDebito);
	}
	
	public Map<Integer, InformacaoDebito> getDebitosAnteriores(){
		return this.debitosAnosAnteriores;
	}
	
	public void addAllInformacaoDebito(Collection<InformacaoDebito> debitos){
		for(InformacaoDebito debito: debitos){
			addInformacaoDebito(debito);
		}		
	}
	
	public String getCodigoContribuinte() {
		return this.codigoContribuinte;
	}

	public void setCodigoContribuinte(String codigoContribuinte) {
		this.codigoContribuinte = codigoContribuinte;
	}

	public Integer getAnoExercicioAtual() {
		return anoExercicioAtual;
	}

	public void setAnoExercicioAtual(Integer anoExercicioAtual) {
		this.anoExercicioAtual = anoExercicioAtual;
	}

	public InformacaoDebito getDebitoExercicioAtual() {
		return debitoExercicioAtual;
	}

	public void setDebitoExercicioAtual(InformacaoDebito debitoExercicioAtual) {
		this.debitoExercicioAtual = debitoExercicioAtual;
	}
		

}
