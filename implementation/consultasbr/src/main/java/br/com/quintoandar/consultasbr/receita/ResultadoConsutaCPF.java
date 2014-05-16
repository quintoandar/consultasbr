package br.com.quintoandar.consultasbr.receita;

import java.io.Serializable;

public class ResultadoConsutaCPF implements Serializable {
	private static final long serialVersionUID = -3571738166207688519L;

	private String nome;
	
	private String codComprovante;

	private StatusCPF status = StatusCPF.INVALIDO;
	
	private byte[] pdf;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCodComprovante() {
		return codComprovante;
	}

	public void setCodComprovante(String codComprovante) {
		this.codComprovante = codComprovante;
	}

	public StatusCPF getStatus() {
		return status;
	}

	public void setStatus(StatusCPF status) {
		this.status = status;
	}

	public byte[] getPdf() {
		return pdf;
	}

	public void setPdf(byte[] pdf) {
		this.pdf = pdf;
	}
}
