package br.com.quintoandar.consultasbr.tjsp;
import java.io.Serializable;
import java.util.Date;


public class ResultadosTJSP implements Serializable {
	private static final long serialVersionUID = -2778427721225058873L;

	private  String codigoProcesso;
	
	private  String codigoExtra;
	
	private  String nome;
	
	private  String envolvimento;
	
	private  Date recebidoEm;
	
	private String vara;
	
	private String tipo;
	
	private String foro;

	private String urlDetalhes;
	
	private String urlBusca;

	public ResultadosTJSP(String searchUrl, String foro) {
		this.foro = foro;
		this.urlBusca = searchUrl;
	}

	public String getCodigoProcesso() {
		return codigoProcesso;
	}

	public void setCodigoProcesso(String codigoProcesso) {
		this.codigoProcesso = codigoProcesso;
	}

	public String getCodigoExtra() {
		return codigoExtra;
	}

	public void setCodigoExtra(String codigoExtra) {
		this.codigoExtra = codigoExtra;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEnvolvimento() {
		return envolvimento;
	}

	public void setEnvolvimento(String envolvimento) {
		this.envolvimento = envolvimento;
	}

	public Date getRecebidoEm() {
		return recebidoEm;
	}

	public void setRecebidoEm(Date recebidoEm) {
		this.recebidoEm = recebidoEm;
	}

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getForo() {
		return foro;
	}

	public void setForo(String foro) {
		this.foro = foro;
	}

	public String getUrlDetalhes() {
		return urlDetalhes;
	}

	public void setUrlDetalhes(String urlDetalhes) {
		this.urlDetalhes = urlDetalhes;
	}

	public String getUrlBusca() {
		return urlBusca;
	}

	public void setUrlBusca(String urlBusca) {
		this.urlBusca = urlBusca;
	}
}
