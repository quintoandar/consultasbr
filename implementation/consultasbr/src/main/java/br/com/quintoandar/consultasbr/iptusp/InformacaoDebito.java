package br.com.quintoandar.consultasbr.iptusp;

import java.util.HashSet;
import java.util.Set;

public class InformacaoDebito{
	
	private Double totalDebito = 0.00D;
	
	private Integer anoExercicio;
		
	private Set<Integer> parcelasEmAberto = new HashSet<Integer>();
	
	private Set<Integer> parcelasVencidas = new HashSet<Integer>();


	public Integer getAnoExercicio() {
		return anoExercicio;
	}

	public void setAnoExercicio(Integer anoExercicio) {
		this.anoExercicio = anoExercicio;
	}

	public Set<Integer> getParcelasEmAberto() {
		return parcelasEmAberto;
	}

	public void setParcelasEmAberto(Set<Integer> numeroParcelasEmAberto) {
		this.parcelasEmAberto = numeroParcelasEmAberto;
	}

	public Double getTotalDebito() {
		return totalDebito;
	}

	public void setTotalDebito(Double totalDebito) {
		this.totalDebito = totalDebito;
	}

	public Set<Integer> getParcelasVencidas() {
		return parcelasVencidas;
	}

	public void setParcelasVencidas(Set<Integer> parcelasVencidas) {
		this.parcelasVencidas = parcelasVencidas;
	}

}
