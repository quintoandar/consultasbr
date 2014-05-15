package br.com.quintoandar.consultasbr.pf;
import java.io.Serializable;

public class ResultadoAntecedentes implements Serializable {
	private static final long serialVersionUID = 8126415741291559230L;

	private StatusAntecedentes status = StatusAntecedentes.Indeterminado;
	
	private String protocolo;

	private byte[] pdf;

	public StatusAntecedentes getStatus() {
		return status;
	}

	public void setStatus(StatusAntecedentes status) {
		this.status = status;
	}
	
	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public byte[] getPdf() {
		return pdf;
	}

	public void setPdf(byte[] pdf) {
		this.pdf = pdf;
	}
	
	
}
