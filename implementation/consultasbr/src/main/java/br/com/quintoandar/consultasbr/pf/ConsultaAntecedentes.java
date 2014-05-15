package br.com.quintoandar.consultasbr.pf;
import java.io.Serializable;


public class ConsultaAntecedentes implements Serializable {
	private static final long serialVersionUID = 3048693054194614137L;

	private String sessionId;
	
	private String nome;
	
	private String cpf;
	
	private String respostaCaptcha;

	public ConsultaAntecedentes(String sessionId, String nome, String cpf, String respostaCaptcha) {
		super();
		this.sessionId = sessionId;
		this.nome = nome;
		this.cpf = cpf;
		this.respostaCaptcha = respostaCaptcha;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getRespostaCaptcha() {
		return respostaCaptcha;
	}

	public void setRespostaCaptcha(String respostaCaptcha) {
		this.respostaCaptcha = respostaCaptcha;
	}
	
	
}
