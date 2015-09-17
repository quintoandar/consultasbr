package br.com.quintoandar.consultasbr.crecisp;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ResultadoCreci implements Serializable {
	private static final long serialVersionUID = 1L;

	private String creci;
	
	private TipoCreci tipo;
	
	private byte[] html;

	private String urlBusca;
	
	private Status status = Status.Inativo;

	public byte[] getHtml() {
		return html;
	}

	public void setHtml(byte[] html) {
		this.html = html;
	}

	public String getUrlBusca() {
		return urlBusca;
	}

	public void setUrlBusca(String urlBusca) {
		this.urlBusca = urlBusca;
	}
	
	public String getCreci() {
		return creci;
	}

	public void setCreci(String creci) {
		this.creci = creci;
	}

	public TipoCreci getTipo() {
		return tipo;
	}

	public void setTipo(TipoCreci tipo) {
		this.tipo = tipo;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}



	public static enum Status {
		Ativo, Inativo;
	}
}
