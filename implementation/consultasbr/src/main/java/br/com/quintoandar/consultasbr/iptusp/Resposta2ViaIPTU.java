package br.com.quintoandar.consultasbr.iptusp;
import java.io.Serializable;
import java.util.Date;

public class Resposta2ViaIPTU implements Serializable {
	
	private static final long serialVersionUID = -4230894962819555816L;

	private String codigoContribuinte;

	private Integer anoExercicio;
	
	private Integer numParcela;

	private String codigo;
	
	private Date vencimento;
	
	private byte[] dado;

	public Resposta2ViaIPTU(String codigoContribuinte, Integer anoExercicio, Integer numParcela) {
		super();
		this.codigoContribuinte = codigoContribuinte;
		this.anoExercicio = anoExercicio;
		this.numParcela = numParcela;
	}

	public String getCodigoContribuinte() {
		return codigoContribuinte;
	}

	public void setCodigoContribuinte(String codigoContribuinte) {
		this.codigoContribuinte = codigoContribuinte;
	}

	public Integer getAnoExercicio() {
		return anoExercicio;
	}

	public void setAnoExercicio(Integer anoExercicio) {
		this.anoExercicio = anoExercicio;
	}

	public Integer getNumParcela() {
		return numParcela;
	}

	public void setNumParcela(Integer numParcela) {
		this.numParcela = numParcela;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public byte[] getDado() {
		return dado;
	}

	public void setDado(byte[] dado) {
		this.dado = dado;
	}
	
	
}
